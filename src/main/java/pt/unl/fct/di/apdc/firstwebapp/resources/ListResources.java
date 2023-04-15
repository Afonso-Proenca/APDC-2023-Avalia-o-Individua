package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.firstwebapp.util.ListData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.ProfileType;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;

@Path("/list")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class ListResources {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG = Logger.getLogger(ListResources.class.getName());

	public ListResources() {
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response listUsers(ListData data) {
		LOG.fine("Attempt to list users by user: " + data.username);

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

			Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").build();
			QueryResults<Entity> results = datastore.run(query);

			List<Map<String, Object>> users = new ArrayList<>();

			while (results.hasNext()) {
				Entity e = results.next();
				Role targetUserRole = Role.valueOf(e.getString("user_role"));
				Estado estado = Estado.valueOf(e.getString("estado"));
				ProfileType profileType = ProfileType.valueOf(e.getString("profileType"));
				Map<String, Object> userDetails = new HashMap<>();

				// Verifica se o usuário logado tem role USER e adiciona apenas os usuarios com
				// role USER, perfil publico e estado ATIVO
				if (role == Role.USER && targetUserRole == Role.USER && estado == Estado.ATIVO
						&& profileType == ProfileType.PUBLIC) {

					userDetails.put("username", e.getString("user_name"));
					userDetails.put("displayName", e.getString("display_name"));
					userDetails.put("email", e.getString("user_email"));

				}
				// Verifica se o usuário logado tem role GBO e adiciona todos os usuários com
				// role USER
				else if (role == Role.GBO && targetUserRole == Role.USER) {

					userDetails.put("username", e.getString("user_name"));
					userDetails.put("displayName", e.getString("display_name"));
					userDetails.put("email", e.getString("user_email"));
					userDetails.put("user_role", e.getString("user_role"));
					userDetails.put("estado", e.getString("estado"));
					userDetails.put("profileType", e.getString("profileType"));
					userDetails.put("phone", e.getString("phone"));
					userDetails.put("mobile_phone", e.getString("mobile_phone"));
					userDetails.put("occupation", e.getString("occupation"));
					userDetails.put("workplace", e.getString("workplace"));
					userDetails.put("address", e.getString("address"));
					userDetails.put("address_complement", e.getString("address_complement"));
					userDetails.put("city", e.getString("city"));
					userDetails.put("postal_code", e.getString("postal_code"));
					userDetails.put("nif", e.getString("nif"));
					userDetails.put("photo", e.getString("photo"));
				}

				// Verifica se o usuário logado tem role GS e adiciona todos os usuários com
				// role USER ou GBO
				else if (role == Role.GS && (targetUserRole == Role.USER || targetUserRole == Role.GBO)) {

					userDetails.put("username", e.getString("user_name"));
					userDetails.put("displayName", e.getString("display_name"));
					userDetails.put("email", e.getString("user_email"));
					userDetails.put("user_role", e.getString("user_role"));
					userDetails.put("estado", e.getString("estado"));
					userDetails.put("profileType", e.getString("profileType"));
					userDetails.put("phone", e.getString("phone"));
					userDetails.put("mobile_phone", e.getString("mobile_phone"));
					userDetails.put("occupation", e.getString("occupation"));
					userDetails.put("workplace", e.getString("workplace"));
					userDetails.put("address", e.getString("address"));
					userDetails.put("address_complement", e.getString("address_complement"));
					userDetails.put("city", e.getString("city"));
					userDetails.put("postal_code", e.getString("postal_code"));
					userDetails.put("nif", e.getString("nif"));
					userDetails.put("photo", e.getString("photo"));
				}
				// Verifica se o usuário logado tem role SU e adiciona todos os usuários
				else if (role == Role.SU) {

					userDetails.put("username", e.getString("user_name"));
					userDetails.put("displayName", e.getString("display_name"));
					userDetails.put("email", e.getString("user_email"));
					userDetails.put("user_role", e.getString("user_role"));
					userDetails.put("estado", e.getString("estado"));
					userDetails.put("profileType", e.getString("profileType"));
					userDetails.put("phone", e.getString("phone"));
					userDetails.put("mobile_phone", e.getString("mobile_phone"));
					userDetails.put("occupation", e.getString("occupation"));
					userDetails.put("workplace", e.getString("workplace"));
					userDetails.put("address", e.getString("address"));
					userDetails.put("address_complement", e.getString("address_complement"));
					userDetails.put("city", e.getString("city"));
					userDetails.put("postal_code", e.getString("postal_code"));
					userDetails.put("nif", e.getString("nif"));
					userDetails.put("photo", e.getString("photo"));
				}

				users.add(userDetails);
			}

			txn.commit();

			return Response.ok(users).build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
}