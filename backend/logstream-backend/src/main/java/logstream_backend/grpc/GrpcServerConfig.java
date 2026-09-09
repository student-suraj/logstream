package logstream_backend.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GrpcServerConfig {

    private Server server;

    @Autowired
    private LogIngestionServiceImpl logIngestionService;

    @PostConstruct
    public void start() throws IOException {

        server = ServerBuilder
                .forPort(9090)
                .addService(logIngestionService)
                .build()
                .start();

        System.out.println("=================================");
        System.out.println("gRPC Server started on port 9090");
        System.out.println("=================================");
    }

    @PreDestroy
    public void stop() {

        if (server != null) {
            server.shutdown();
        }
    }
}