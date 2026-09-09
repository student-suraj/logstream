package logstream_backend.lucene;

import com.logstream.grpc.LogMessage;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Int64Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class LuceneIndexService {

    private static final String INDEX_PATH = "logs-index";

    private final StandardAnalyzer analyzer;

    public LuceneIndexService() {
        this.analyzer = new StandardAnalyzer();
    }

    public void indexLog(LogMessage logMessage) {

        try (Directory directory = FSDirectory.open(Path.of(INDEX_PATH))) {

            IndexWriterConfig config = new IndexWriterConfig(analyzer);

            try (IndexWriter writer = new IndexWriter(directory, config)) {

                Document document = new Document();

                document.add(new StringField(
                        "timestamp",
                        logMessage.getTimestamp(),
                        StringField.Store.YES));

                document.add(new StringField(
                        "level",
                        logMessage.getLevel(),
                        StringField.Store.YES));

                document.add(new StringField(
                        "service",
                        logMessage.getService(),
                        StringField.Store.YES));

                document.add(new TextField(
                        "message",
                        logMessage.getMessage(),
                        TextField.Store.YES));

                document.add(new Int64Field(
                        "responseTime",
                        logMessage.getResponseTime(),
                        Int64Field.Store.YES));

                writer.addDocument(document);
                writer.commit();

                System.out.println("Log indexed successfully in Lucene");

            }

        } catch (IOException e) {

            System.err.println(
                    "Failed to index log: " + e.getMessage());

        }
    }
}