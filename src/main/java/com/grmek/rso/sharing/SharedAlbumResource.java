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
@Path("shared-albums")
@Log
public class SharedAlbumResource {

    @Inject
    private ConfigurationProperties cfg;

    @POST
    public Response addNewSharedAlbum(@QueryParam("shared-with-user") int sharedWithUserId,
                                      SharedAlbum sharedAlbum) {
        try (
            Connection con = DriverManager.getConnection(cfg.getDbUrl(), cfg.getDbUser(), cfg.getDbPassword());
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM sharing_data WHERE user_id = "
                                             + sharedAlbum.getUserId() + " AND album_id = " + sharedAlbum.getAlbumId()
                                             + " AND shared_with_user_id = " + sharedWithUserId);
        ) {
            if (!rs.next()) {
                stmt.executeUpdate("INSERT INTO sharing_data (user_id, album_id, shared_with_user_id) VALUES ('"
                                   + sharedAlbum.getUserId() + "', '" + sharedAlbum.getAlbumId() + "', '"
                                   + sharedWithUserId + "')");
            }
        }
        catch (SQLException e) {
            System.err.println(e);
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.noContent().build();
    }

    @GET
    public Response getSharedAlbum(@QueryParam("shared-with-user") int sharedWithUserId) {
        List<SharedAlbum> sharedAlbums = new LinkedList<SharedAlbum>();

        try (
                Connection con = DriverManager.getConnection(cfg.getDbUrl(), cfg.getDbUser(), cfg.getDbPassword());
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM sharing_data WHERE shared_with_user_id = "
                                                 + sharedWithUserId);
        ) {
            while (rs.next()) {
                SharedAlbum sharedAlbum = new SharedAlbum();
                sharedAlbum.setId(rs.getString(1));
                sharedAlbum.setUserId(rs.getString(2));
                sharedAlbum.setAlbumId(rs.getString(3));
                sharedAlbums.add(sharedAlbum);
            }
        }
        catch (SQLException e) {
            System.err.println(e);
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.ok(sharedAlbums).build();
    }
    
    @DELETE
    public Response deleteSharedAlbum(@QueryParam("shared-with-user") int sharedWithUserId,
                                      SharedAlbum sharedAlbum) {
        int userId = Integer.parseInt(sharedAlbum.getUserId());
        int albumId = Integer.parseInt(sharedAlbum.getAlbumId());

        try (
            Connection con = DriverManager.getConnection(cfg.getDbUrl(), cfg.getDbUser(), cfg.getDbPassword());
            Statement stmt = con.createStatement();
        ) {
            stmt.executeUpdate("DELETE FROM sharing_data WHERE user_id = " + userId + " AND album_id = "
                               + albumId + " AND shared_with_user_id = " + sharedWithUserId);
        }
        catch (SQLException e) {
            System.err.println(e);
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.noContent().build();
    }
}
