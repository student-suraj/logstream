package logstream_backend.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class LuceneSearchService {

    private static final String INDEX_PATH = "logs-index";

    private final StandardAnalyzer analyzer = new StandardAnalyzer();

    public List<Document> search(String field, String searchText) {

        List<Document> results = new ArrayList<>();

        try (Directory directory = FSDirectory.open(Path.of(INDEX_PATH));
                IndexReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);

            Query query;

            // Exact match for structured fields
            if (field.equals("service")
                    || field.equals("level")
                    || field.equals("timestamp")) {

                query = new TermQuery(
                        new Term(field, searchText));

            } else {

                // Full-text search for message
                QueryParser parser = new QueryParser(field, analyzer);

                query = parser.parse(searchText);
            }

            TopDocs topDocs = searcher.search(query, 20);

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                results.add(searcher.doc(scoreDoc.doc));
            }

        } catch (Exception e) {
            System.err.println(
                    "Search failed: " + e.getMessage());
        }

        return results;
    }
}