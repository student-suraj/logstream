package logstream_backend.controller;

import logstream_backend.alerts.AlertRuleService;
import logstream_backend.alerts.AlertRuleService.AlertRule;
import logstream_backend.alerts.AlertRuleService.CreateRuleRequest;
import logstream_backend.alerts.AlertRuleService.TriggeredAlert;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class AlertController {
    private final AlertRuleService alertRuleService;

    public AlertController(AlertRuleService alertRuleService) {
        this.alertRuleService = alertRuleService;
    }

    @GetMapping("/rules")
    public List<AlertRule> rules() {
        return alertRuleService.listRules();
    }

    @PostMapping("/rules")
    @ResponseStatus(HttpStatus.CREATED)
    public AlertRule createRule(@RequestBody CreateRuleRequest request) {
        try {
            return alertRuleService.create(request);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @DeleteMapping("/rules/{id}")
    public Map<String, Object> deleteRule(@PathVariable String id) {
        if (!alertRuleService.delete(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert rule not found.");
        }
        return Map.of("deleted", true, "id", id);
    }

    @GetMapping
    public List<TriggeredAlert> alerts() {
        return alertRuleService.listAlerts();
    }
}