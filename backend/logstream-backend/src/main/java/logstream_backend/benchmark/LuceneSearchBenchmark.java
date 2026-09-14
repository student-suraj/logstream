package logstream_backend.benchmark;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class LuceneSearchBenchmark {

    private static final int LOG_COUNT = 1_000_000;
    private static final String INDEX_PATH = "benchmark-index";

    public static void main(String[] args) throws Exception {

        Path indexPath = Path.of(INDEX_PATH);

        System.out.println("=================================");
        System.out.println("Creating 1,000,000 log documents...");
        System.out.println("=================================");

        // Delete old benchmark index
        if (Files.exists(indexPath)) {
            try (var files = Files.walk(indexPath)) {
                files.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }

        StandardAnalyzer analyzer = new StandardAnalyzer();

        // -----------------------------------------
        // STEP 1: INDEX 1,000,000 LOGS
        // -----------------------------------------

        long indexingStart = System.nanoTime();

        try (Directory directory = FSDirectory.open(indexPath);
                IndexWriter writer = new IndexWriter(
                        directory,
                        new IndexWriterConfig(analyzer))) {

            for (int i = 0; i < LOG_COUNT; i++) {

                Document document = new Document();

                document.add(new StringField(
                        "service",
                        "payment-service",
                        StringField.Store.YES));

                document.add(new StringField(
                        "level",
                        i % 2 == 0 ? "INFO" : "ERROR",
                        StringField.Store.YES));

                document.add(new TextField(
                        "message",
                        "Payment processed successfully " + i,
                        TextField.Store.YES));

                writer.addDocument(document);

                // Progress every 100,000 logs
                if ((i + 1) % 100_000 == 0) {
                    System.out.println(
                            "Indexed: " + (i + 1) + " logs");
                }
            }

            writer.commit();
        }

        long indexingEnd = System.nanoTime();

        double indexingTime = (indexingEnd - indexingStart) / 1_000_000_000.0;

        // -----------------------------------------
        // STEP 2: SEARCH BENCHMARK
        // -----------------------------------------

        try (Directory directory = FSDirectory.open(indexPath);
                IndexReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);

            Query query = new TermQuery(
                    new Term(
                            "service",
                            "payment-service"));

            // Warm-up search
            searcher.search(query, 20);

            // Actual search measurement
            long searchStart = System.nanoTime();

            TopDocs results = searcher.search(query, 20);

            long searchEnd = System.nanoTime();

            double searchTime = (searchEnd - searchStart) / 1_000_000.0;

            System.out.println();
            System.out.println("=================================");
            System.out.println("LogStream Search Benchmark");
            System.out.println("=================================");
            System.out.println(
                    "Indexed documents : "
                            + reader.numDocs());
            System.out.println(
                    "Results returned  : "
                            + results.scoreDocs.length);
            System.out.printf(
                    "Indexing time     : %.3f seconds%n",
                    indexingTime);
            System.out.printf(
                    "Search time       : %.3f ms%n",
                    searchTime);

            if (searchTime < 50) {
                System.out.println(
                        "Target (<50 ms)   : PASSED");
            } else {
                System.out.println(
                        "Target (<50 ms)   : NOT MET");
            }

            System.out.println("=================================");
        }

        analyzer.close();
    }
}