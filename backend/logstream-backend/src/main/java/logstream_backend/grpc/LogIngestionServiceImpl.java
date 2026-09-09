package logstream_backend.grpc;

import com.logstream.grpc.LogIngestionServiceGrpc;
import com.logstream.grpc.LogMessage;
import com.logstream.grpc.LogResponse;
import io.grpc.stub.StreamObserver;
import logstream_backend.lucene.LuceneIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogIngestionServiceImpl
        extends LogIngestionServiceGrpc.LogIngestionServiceImplBase {

    private final LuceneIndexService luceneIndexService;

    @Autowired
    public LogIngestionServiceImpl(LuceneIndexService luceneIndexService) {
        this.luceneIndexService = luceneIndexService;
    }

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

        // Index the received log into Apache Lucene
        luceneIndexService.indexLog(request);

        LogResponse response = LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Log received and indexed successfully")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}