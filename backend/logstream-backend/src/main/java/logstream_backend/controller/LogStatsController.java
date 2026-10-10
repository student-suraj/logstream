package logstream_backend.controller;

import logstream_backend.lucene.LuceneSearchService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class LogStatsController {
    private final LuceneSearchService searchService;
    public LogStatsController(LuceneSearchService searchService) { this.searchService = searchService; }

    @GetMapping("/stats")
    public Map<String, Object> stats() { return searchService.stats(); }
}