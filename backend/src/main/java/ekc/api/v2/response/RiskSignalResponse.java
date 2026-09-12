package ekc.api.v2.response;

import ekc.workspace.change.RiskSignal;

import java.util.List;

public record RiskSignalResponse(
        String category,
        String severity,
        String title,
        String description,
        List<String> evidence) {
    static RiskSignalResponse from(RiskSignal signal) {
        return new RiskSignalResponse(
                signal.category(), signal.severity(), signal.title(),
                signal.description(), signal.evidence());
    }
}
