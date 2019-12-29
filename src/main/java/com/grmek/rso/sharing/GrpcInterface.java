package com.grmek.rso.sharing;

import com.kumuluz.ee.grpc.annotations.GrpcService;
import io.grpc.stub.StreamObserver;
import javax.enterprise.inject.spi.CDI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

@GrpcService
public class GrpcInterface extends SharingGrpc.SharingImplBase {

    /* CDI injection currently doesn't work inside service implementations. Lookup is done manually. */
    ConfigurationProperties cfg;

    @Override
    public void userCleanUp(SharingService.CleanUpRequest request,
                            StreamObserver<SharingService.CleanUpResponse> responseObserver) {
        Integer status = 0;

        cfg = CDI.current().select(ConfigurationProperties.class).get();

        try (
            Connection con = DriverManager.getConnection(cfg.getDbUrl(), cfg.getDbUser(), cfg.getDbPassword());
            Statement stmt = con.createStatement();
        ) {
            stmt.executeUpdate("DELETE FROM sharing_data WHERE user_id = "
                               + request.getId() + " OR shared_with_user_id = " + request.getId());
        }
        catch (SQLException e) {
            System.err.println(e);
            status = -1;
        }

        SharingService.CleanUpResponse response = SharingService.CleanUpResponse.newBuilder().setStatus(status).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void albumCleanUp(SharingService.CleanUpRequest request,
                             StreamObserver<SharingService.CleanUpResponse> responseObserver) {
        Integer status = 0;

        cfg = CDI.current().select(ConfigurationProperties.class).get();

        try (
            Connection con = DriverManager.getConnection(cfg.getDbUrl(), cfg.getDbUser(), cfg.getDbPassword());
            Statement stmt = con.createStatement();
        ) {
            stmt.executeUpdate("DELETE FROM sharing_data WHERE album_id = " + request.getId());
        }
        catch (SQLException e) {
            System.err.println(e);
            status = -1;
        }

        SharingService.CleanUpResponse response = SharingService.CleanUpResponse.newBuilder().setStatus(status).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
