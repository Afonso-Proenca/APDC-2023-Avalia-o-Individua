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
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.ProfileType;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class RegisterResource {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());

	public RegisterResource() {
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegistration(RegisterData data) {
		LOG.fine("Attempt to register user: " + data.username);
		if (!data.validRegistration()) {
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
		}
		Transaction txn = datastore.newTransaction();
		try {

			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);
			if (user != null) {
				txn.rollback();

				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			} else {

				user = Entity.newBuilder(userKey).set("display_name", data.displayName).set("user_name", data.username)
						.set("user_pwd", DigestUtils.sha512Hex(data.password)).set("user_email", data.email)
						.set("confirm_pwd", data.confirmPwd).set("user_role", Role.USER.toString())
						.set("estado", Estado.INATIVO.toString()).set("profileType", ProfileType.PUBLIC.toString())
						.set("phone", "").set("mobile_phone", "").set("occupation", "").set("workplace", "")
						.set("address", "").set("address_complement", "").set("city", "").set("postal_code", "")
						.set("nif", "").set("photo", "").set("user_creation_time", Timestamp.now()).build();

				txn.add(user);
				LOG.info("User registered" + data.username);
				txn.commit();
				return Response.ok("User registered").build();
			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();

			}
		}

	}

}
