package logstream_backend.alerts;

import com.logstream.grpc.LogMessage;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Locale;

@Service
public class AlertRuleService {
    private final Map<String, AlertRule> rules = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<TriggeredAlert> recentAlerts = new CopyOnWriteArrayList<>();

    public record AlertRule(String id, String name, String level, String service,
                            String messageContains, boolean enabled, String createdAt) {}
    public record TriggeredAlert(String id, String ruleId, String ruleName, String timestamp,
                                 String level, String service, String message, String triggeredAt) {}
    public record CreateRuleRequest(String name, String level, String service,
                                    String messageContains, Boolean enabled) {}

    public AlertRule create(CreateRuleRequest request) {
        if (request == null || blank(request.name())) {
            throw new IllegalArgumentException("Rule name is required.");
        }
        String level = clean(request.level()).toUpperCase(Locale.ROOT);
        String service = clean(request.service());
        String message = clean(request.messageContains());
        if (level.isEmpty() && service.isEmpty() && message.isEmpty()) {
            throw new IllegalArgumentException("Provide at least one condition: level, service, or messageContains.");
        }
        if (!level.isEmpty() && !List.of("TRACE", "DEBUG", "INFO", "WARN", "WARNING", "ERROR", "FATAL").contains(level)) {
            throw new IllegalArgumentException("Unsupported log level.");
        }
        AlertRule rule = new AlertRule(UUID.randomUUID().toString(), request.name().trim(),
                level, service, message, request.enabled() == null || request.enabled(), Instant.now().toString());
        rules.put(rule.id(), rule);
        return rule;
    }

    public List<AlertRule> listRules() {
        return rules.values().stream().sorted((a, b) -> a.createdAt().compareTo(b.createdAt())).toList();
    }

    public boolean delete(String id) {
        return rules.remove(id) != null;
    }

    public List<TriggeredAlert> listAlerts() {
        return new ArrayList<>(recentAlerts);
    }

    public void evaluate(LogMessage log) {
        String level = log.getLevel().toUpperCase(Locale.ROOT);
        for (AlertRule rule : rules.values()) {
            if (!rule.enabled()) continue;
            if (!rule.level().isEmpty() && !rule.level().equals(level)) continue;
            if (!rule.service().isEmpty() && !rule.service().equalsIgnoreCase(log.getService())) continue;
            if (!rule.messageContains().isEmpty()
                    && !log.getMessage().toLowerCase(Locale.ROOT).contains(rule.messageContains().toLowerCase(Locale.ROOT))) continue;

            recentAlerts.add(0, new TriggeredAlert(UUID.randomUUID().toString(), rule.id(), rule.name(),
                    log.getTimestamp(), log.getLevel(), log.getService(), log.getMessage(), Instant.now().toString()));
            while (recentAlerts.size() > 500) recentAlerts.remove(recentAlerts.size() - 1);
        }
    }

    private static String clean(String value) { return value == null ? "" : value.trim(); }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
}