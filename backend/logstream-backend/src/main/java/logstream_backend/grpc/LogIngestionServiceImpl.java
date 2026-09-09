package logstream_backend.grpc;

import com.logstream.grpc.LogIngestionServiceGrpc;
import com.logstream.grpc.LogMessage;
import com.logstream.grpc.LogResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class LogIngestionServiceImpl
        extends LogIngestionServiceGrpc.LogIngestionServiceImplBase {

    @Override
    public void sendLog(
            LogMessage request,
            StreamObserver<LogResponse> responseObserver) {

        System.out.println("----- Incoming Log -----");
        System.out.println("Timestamp: " + request.getTimestamp());
        System.out.println("Level: " + request.getLevel());
        System.out.println("Service: " + request.getService());
        System.out.println("Message: " + request.getMessage());
        System.out.println("Response Time: " + request.getResponseTime());

        LogResponse response = LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Log received successfully")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}