package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;



//OP extra

@Path("/order")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class OrderUsersResource {

    private static final Logger LOG = Logger.getLogger(OrderUsersResource.class.getName());
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public OrderUsersResource() {
    }

    @GET
    @Path("/")
    public Response orderUsers() {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("User")
                .setOrderBy(OrderBy.asc("user_name"))
                .build();

        QueryResults<Entity> resultList = datastore.run(query);
        List<String> orderedUsers = new ArrayList<>();

        while (resultList.hasNext()) {
            Entity user = resultList.next();
            orderedUsers.add(user.getString("user_name"));
        }

        // Log the ordered users
        LOG.info("Ordered users: " + orderedUsers.toString());

        if (orderedUsers.isEmpty()) {
            return Response.status(Status.NOT_FOUND).entity("No users found.").build();
        } else {
      
            return Response.ok(orderedUsers.toString()).build();
        }
    }
}
