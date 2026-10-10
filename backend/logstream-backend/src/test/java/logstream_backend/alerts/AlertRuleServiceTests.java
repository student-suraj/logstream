package logstream_backend.alerts;

import com.logstream.grpc.LogMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertRuleServiceTests {
    @Test
    void matchingErrorLogCreatesTriggeredAlert() {
        AlertRuleService service = new AlertRuleService();
        service.create(new AlertRuleService.CreateRuleRequest(
                "Payment errors", "error", "payment-service", "declined", true));

        service.evaluate(LogMessage.newBuilder()
                .setTimestamp("2026-10-10T10:00:00Z")
                .setLevel("ERROR")
                .setService("PAYMENT-SERVICE")
                .setMessage("Payment declined by processor")
                .setResponseTime(120)
                .build());

        assertEquals(1, service.listAlerts().size());
        assertEquals("Payment errors", service.listAlerts().get(0).ruleName());
    }

    @Test
    void nonMatchingLogDoesNotTriggerAlert() {
        AlertRuleService service = new AlertRuleService();
        service.create(new AlertRuleService.CreateRuleRequest(
                "Only errors", "ERROR", "", "", true));

        service.evaluate(LogMessage.newBuilder()
                .setLevel("INFO")
                .setService("catalog")
                .setMessage("Item loaded")
                .build());

        assertTrue(service.listAlerts().isEmpty());
    }

    @Test
    void ruleRequiresAtLeastOneCondition() {
        AlertRuleService service = new AlertRuleService();
        assertThrows(IllegalArgumentException.class, () -> service.create(
                new AlertRuleService.CreateRuleRequest("Empty rule", "", "", "", true)));
    }
}