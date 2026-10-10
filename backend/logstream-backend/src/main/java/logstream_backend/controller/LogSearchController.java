package logstream_backend.controller;

import logstream_backend.lucene.LuceneSearchService;
import org.apache.lucene.document.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "http://localhost:5173")
public class LogSearchController {

    private static final Set<String> ALLOWED_FIELDS =
            Set.of("service", "level", "message", "timestamp");

    private final LuceneSearchService searchService;

    public LogSearchController(LuceneSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public List<Map<String, String>> searchLogs(
            @RequestParam String field,
            @RequestParam String query) {

        if (!ALLOWED_FIELDS.contains(field)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported field. Choose service, level, message, or timestamp.");
        }

        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Search query must not be blank.");
        }

        List<Document> documents = searchService.search(field, query.trim());

        return documents.stream()
                .map(document -> Map.of(
                        "timestamp", safeValue(document, "timestamp"),
                        "level", safeValue(document, "level"),
                        "service", safeValue(document, "service"),
                        "message", safeValue(document, "message"),
                        "responseTime", safeValue(document, "responseTime")))
                .toList();
    }

    private static String safeValue(Document document, String field) {
        String value = document.get(field);
        return value == null ? "" : value;
    }
}
