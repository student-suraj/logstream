package logstream_backend.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;

@Service
public class LuceneSearchService {
    private static final String INDEX_PATH = "logs-index";
    private final StandardAnalyzer analyzer = new StandardAnalyzer();

    public List<Document> search(String field, String searchText) {
        List<Document> results = new ArrayList<>();
        if (field == null || searchText == null || searchText.isBlank()) return results;
        try (Directory directory = FSDirectory.open(Path.of(INDEX_PATH))) {
            if (!DirectoryReader.indexExists(directory)) return results;
            try (IndexReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                Query query;
                if (field.equals("service") || field.equals("level") || field.equals("timestamp")) {
                    query = new TermQuery(new Term(field, searchText));
                } else {
                    query = new QueryParser(field, analyzer).parse(searchText);
                }
                TopDocs topDocs = searcher.search(query, 20);
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) results.add(searcher.doc(scoreDoc.doc));
            }
        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage());
        }
        return results;
    }

    public Map<String, Object> stats() {
        Map<String, Long> byLevel = new TreeMap<>();
        Map<String, Long> byService = new TreeMap<>();
        long total = 0;
        long responseTimeTotal = 0;
        long responseTimeCount = 0;
        try (Directory directory = FSDirectory.open(Path.of(INDEX_PATH))) {
            if (DirectoryReader.indexExists(directory)) {
                try (IndexReader reader = DirectoryReader.open(directory)) {
                    total = reader.numDocs();
                    IndexSearcher searcher = new IndexSearcher(reader);
                    for (int i = 0; i < reader.maxDoc(); i++) {
                        Document doc = searcher.doc(i);
                        byLevel.merge(value(doc, "level", "UNKNOWN").toUpperCase(Locale.ROOT), 1L, Long::sum);
                        byService.merge(value(doc, "service", "unknown"), 1L, Long::sum);
                        try {
                            responseTimeTotal += Long.parseLong(value(doc, "responseTime", ""));
                            responseTimeCount++;
                        } catch (NumberFormatException ignored) { }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not read Lucene stats: " + e.getMessage());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalLogs", total);
        result.put("byLevel", byLevel);
        result.put("byService", byService);
        result.put("averageResponseTimeMs", responseTimeCount == 0 ? 0 : responseTimeTotal / responseTimeCount);
        return result;
    }

    private static String value(Document document, String field, String fallback) {
        String value = document.get(field);
        return value == null || value.isBlank() ? fallback : value;
    }
}