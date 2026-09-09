package logstream_backend.client;

import com.logstream.grpc.LogIngestionServiceGrpc;
import com.logstream.grpc.LogMessage;
import com.logstream.grpc.LogResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class LogGrpcClient {

    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        LogIngestionServiceGrpc.LogIngestionServiceBlockingStub stub = LogIngestionServiceGrpc.newBlockingStub(channel);

        LogMessage log = LogMessage.newBuilder()
                .setTimestamp("2026-09-09T23:45:00")
                .setLevel("INFO")
                .setService("payment-service")
                .setMessage("Payment processed successfully")
                .setResponseTime(120)
                .build();

        LogResponse response = stub.sendLog(log);

        System.out.println("Response: " + response.getMessage());
        System.out.println("Success: " + response.getSuccess());

        channel.shutdown();
    }
}