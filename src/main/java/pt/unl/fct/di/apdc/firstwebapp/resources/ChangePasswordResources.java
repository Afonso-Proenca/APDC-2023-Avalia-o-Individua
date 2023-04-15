package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.firstwebapp.util.ChangePasswordData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;

@Path("/changePassword")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class ChangePasswordResources {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private static final Logger LOG = Logger.getLogger(ChangePasswordResources.class.getName());

	public ChangePasswordResources() {
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePassword(ChangePasswordData data) {
		LOG.fine("Attempt to change password for user: " + data.username);

		if (!data.newPassword.equals(data.confirmNewPassword)) {
			return Response.status(Status.BAD_REQUEST).entity("New passwords do not match.").build();
		}

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);

			if (user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User not found.").build();
			}

		    Estado userStatus = Estado.valueOf(user.getString("estado"));

            if (userStatus == Estado.INATIVO) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("Inactive user cannot proceed.").build();
            }
            
			
			String storedPassword = user.getString("user_pwd");
			if (!DigestUtils.sha512Hex(data.password).equals(storedPassword)) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Incorrect current password.").build();
			}

			user = Entity.newBuilder(userKey).set("user_pwd", DigestUtils.sha512Hex(data.newPassword)).build();

			txn.put(user);
			LOG.info("Password changed for user: " + data.username);
			txn.commit();
			return Response.ok("Password changed").build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
}
