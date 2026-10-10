package logstream_backend.grpc;

import com.logstream.grpc.LogIngestionServiceGrpc;
import com.logstream.grpc.LogMessage;
import com.logstream.grpc.LogResponse;
import io.grpc.stub.StreamObserver;
import logstream_backend.alerts.AlertRuleService;
import logstream_backend.lucene.LuceneIndexService;
import org.springframework.stereotype.Service;

@Service
public class LogIngestionServiceImpl extends LogIngestionServiceGrpc.LogIngestionServiceImplBase {
    private final LuceneIndexService luceneIndexService;
    private final AlertRuleService alertRuleService;

    public LogIngestionServiceImpl(LuceneIndexService luceneIndexService, AlertRuleService alertRuleService) {
        this.luceneIndexService = luceneIndexService;
        this.alertRuleService = alertRuleService;
    }

    @Override
    public void sendLog(LogMessage request, StreamObserver<LogResponse> responseObserver) {
        try {
            luceneIndexService.indexLog(request);
            alertRuleService.evaluate(request);
            responseObserver.onNext(LogResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Log received, indexed, and evaluated against alert rules.")
                    .build());
            responseObserver.onCompleted();
        } catch (RuntimeException exception) {
            responseObserver.onNext(LogResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Log processing failed: " + exception.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }
}