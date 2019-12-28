package com.grmek.rso.sharing;

import com.kumuluz.ee.logs.cdi.Log;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("users-with-access")
@Log
public class UsersWithAccessResource {

    @Inject
    private ConfigurationProperties cfg;

    @GET
    public Response getUsersWithAccess(@QueryParam("user") int userId,
                                       @QueryParam("album") int albumId) {
        List<String> usersWithAccess = new LinkedList<String>();

        try (
            Connection con = DriverManager.getConnection(cfg.getDbUrl(), cfg.getDbUser(), cfg.getDbPassword());
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM sharing_data WHERE user_id = "
                                             + userId + " AND album_id = " + albumId);
        ) {
            while (rs.next()) {
                String userWithAccess = rs.getString(4);
                usersWithAccess.add(userWithAccess);
            }
        }
        catch (SQLException e) {
            System.err.println(e);
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.ok(usersWithAccess).build();
    }
}
