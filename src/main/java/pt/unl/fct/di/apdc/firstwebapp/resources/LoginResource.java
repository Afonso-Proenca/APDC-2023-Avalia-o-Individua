package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;

import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	/*
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private final Gson g = new Gson();

	public LoginResource() {
	}

	@POST

	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
	public Response doLogin1(LoginData data) {
		LOG.fine("Attempt to login user: " + data.username);
		Transaction txn = datastore.newTransaction();
		try {

			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = datastore.get(userKey);

			if (user != null) {
				String hashedPWD = user.getString("user_pwd");
				if (hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
					AuthToken token = new AuthToken(data.username);

					/*
					 * Estado userStatus = Estado.valueOf(user.getString("estado"));
					 * 
					 * if (userStatus == Estado.ATIVO) { txn.rollback(); return
					 * Response.status(Status.BAD_REQUEST).entity("User was already logged in.").
					 * build(); }
					 */

					Entity updateUser = Entity.newBuilder(user).set("user_token", token.tokenID.toString())
							.set("user_token_expiration", token.expirationData).set("estado", Estado.ATIVO.toString())
							.build();

					Map<String, Object> responseData = new HashMap<>();
					responseData.put("displayName", user.getString("display_name"));
					responseData.put("username", user.getString("user_name"));
					responseData.put("mobilePhone", user.getString("mobile_phone"));
					responseData.put("email", user.getString("user_email"));
					responseData.put("role", user.getString("user_role"));
					responseData.put("phone", user.getString("phone"));
					responseData.put("profileVisibility", user.getString("profileType"));
					responseData.put("state", Estado.ATIVO.toString());
					responseData.put("occupation", user.getString("occupation"));
					responseData.put("workplace", user.getString("workplace"));
					responseData.put("address", user.getString("address"));
					responseData.put("additionalAddress", user.getString("address_complement"));
					responseData.put("postalCode", user.getString("postal_code"));
					responseData.put("nif", user.getString("nif"));
					responseData.put("photo", user.getString("photo"));

					txn.put(updateUser);

					txn.commit();

					LOG.info("User '" + data.username + "' logged in sucessfully.");

					return Response.ok("You are logged in here is the token " + g.toJson(token).toString()
							+ g.toJson(responseData)).build();
				} else {
					LOG.warning("Wrong password for username: " + data.username);
					txn.rollback();
					return Response.status(Status.FORBIDDEN).entity("Wrong password.").build();
				}

			}

			else {

				LOG.warning("Failed login attempt for username: " + data.username);
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Username does not exist.").build();

			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();

			}
		}
	}

}
