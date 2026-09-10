package logstream_backend.controller;

import logstream_backend.lucene.LuceneSearchService;
import org.apache.lucene.document.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "http://localhost:5173")
public class LogSearchController {

    private final LuceneSearchService searchService;

    public LogSearchController(LuceneSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public List<Map<String, String>> searchLogs(
            @RequestParam String field,
            @RequestParam String query) {

        List<Document> documents = searchService.search(field, query);

        return documents.stream()
                .map(document -> Map.of(
                        "timestamp", document.get("timestamp"),
                        "level", document.get("level"),
                        "service", document.get("service"),
                        "message", document.get("message"),
                        "responseTime", document.get("responseTime")))
                .toList();
    }
}