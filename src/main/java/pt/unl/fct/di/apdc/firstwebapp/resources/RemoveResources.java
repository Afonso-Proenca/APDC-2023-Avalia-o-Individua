package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.firstwebapp.util.RemoverData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;

@Path("/remove")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class RemoveResources {

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    private static final Logger LOG = Logger.getLogger(RemoveResources.class.getName());

    public RemoveResources() {
    }

    @DELETE
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUser(RemoverData data) {
        LOG.fine("Attempt to remove user: " + data.targetUsername);

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
            
            
            
            Role role = Role.valueOf(user.getString("user_role"));

            if (role == Role.USER && !data.username.equals(data.targetUsername)) {
                txn.rollback();
                return Response.status(Status.FORBIDDEN).entity("User can only remove their own account.").build();
            }

            Key targetUserKey = datastore.newKeyFactory().setKind("User").newKey(data.targetUsername);
            Entity targetUser = txn.get(targetUserKey);

            if (targetUser == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("Target user not found.").build();
            }

            // Fetch the role of the target user from the datastore
            Role targetUserRole = Role.valueOf(targetUser.getString("user_role"));

            if (role == Role.GBO && targetUserRole != Role.USER) {
                txn.rollback();
                return Response.status(Status.FORBIDDEN).entity("GBO can only remove accounts with role USER.").build();
            }

            if (role == Role.GS && (targetUserRole == Role.SU)) {
                txn.rollback();
                return Response.status(Status.FORBIDDEN).entity("GS can only remove accounts with role USER or GBO.").build();
            }

            txn.delete(targetUserKey);
            LOG.info("User removed: " + data.targetUsername);
            txn.commit();
            return Response.ok("User removed").build();

        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
}