
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

import pt.unl.fct.di.apdc.firstwebapp.util.ModifyData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;

@Path("/modify")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class ModifyResources {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG = Logger.getLogger(ModifyResources.class.getName());

	public ModifyResources() {
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyUser(ModifyData data) {
		LOG.fine("Attempt to modify user: " + data.targetUsername + " by user: " + data.username);

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);

			if (user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User not found.").build();
			}

			String storedPassword = user.getString("user_pwd");
			if (!DigestUtils.sha512Hex(data.password).equals(storedPassword)) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Incorrect password.").build();
			}

			Estado userStatus = Estado.valueOf(user.getString("estado"));

			if (userStatus == Estado.INATIVO) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Inactive user cannot proceed.").build();
			}

			Role role = Role.valueOf(user.getString("user_role"));

			Key targetUserKey = datastore.newKeyFactory().setKind("User").newKey(data.targetUsername);
			Entity targetUser = txn.get(targetUserKey);

			if (targetUser == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Target user not found.").build();
			}
			Entity.Builder updatedUserBuilder = Entity.newBuilder(targetUser);
			Role targetUserRole = Role.valueOf(targetUser.getString("user_role"));

			boolean canModify = false;

			if (role == Role.USER && data.username.equals(data.targetUsername)) {
				canModify = true;
			} else if (role == Role.GBO && targetUserRole == Role.USER) {
				canModify = true;

				if (data.displayName != null) {
					updatedUserBuilder.set("display_name", data.displayName);
				}

				if (data.email != null) {
					updatedUserBuilder.set("user_email", data.email);
				}

				if (data.role != null) {
					updatedUserBuilder.set("user_role", data.role.toString());
				}
				if (data.estado != null) {
					updatedUserBuilder.set("estado", data.estado.toString());
				}

			} else if (role == Role.GS && (targetUserRole == Role.USER || targetUserRole == Role.GBO)) {
				canModify = true;

				if (data.displayName != null) {
					updatedUserBuilder.set("display_name", data.displayName);
				}

				if (data.email != null) {
					updatedUserBuilder.set("user_email", data.email);
				}

				if (data.role != null) {
					updatedUserBuilder.set("user_role", data.role.toString());
				}
				if (data.estado != null) {
					updatedUserBuilder.set("estado", data.estado.toString());
				}
			} else if (role == Role.SU) {
				canModify = true;

				if (data.displayName != null) {
					updatedUserBuilder.set("display_name", data.displayName);
				}

				if (data.email != null) {
					updatedUserBuilder.set("user_email", data.email);
				}

				if (data.role != null) {
					updatedUserBuilder.set("user_role", data.role.toString());
				}
				if (data.estado != null) {
					updatedUserBuilder.set("estado", data.estado.toString());
				}
			}

			if (canModify) {

				if (data.profileType != null) {
					updatedUserBuilder.set("profileType", data.profileType.toString());
				}
				if (data.phone != null) {
					updatedUserBuilder.set("phone", data.phone);
				}
				if (data.mobilePhone != null) {
					updatedUserBuilder.set("mobile_Phone", data.mobilePhone);
				}
				if (data.occupation != null) {
					updatedUserBuilder.set("occupation", data.occupation);
				}
				if (data.workplace != null) {
					updatedUserBuilder.set("workplace", data.workplace);
				}
				if (data.address != null) {
					updatedUserBuilder.set("address", data.address);
				}
				if (data.addressComplement != null) {
					updatedUserBuilder.set("address_Complement", data.addressComplement);
				}
				if (data.city != null) {
					updatedUserBuilder.set("city", data.city);
				}
				if (data.postalCode != null) {
					updatedUserBuilder.set("postal_code", data.postalCode);
				}
				if (data.nif != null) {
					updatedUserBuilder.set("nif", data.nif);
				}
				if (data.photo != null) {
					updatedUserBuilder.set("photo", data.photo);
				}

				Entity updatedUser = updatedUserBuilder.build();
				txn.put(updatedUser);
				txn.commit();
				return Response.ok("User modified").build();
			} else {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("You do not have permission to modify this user.")
						.build();
			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
}
