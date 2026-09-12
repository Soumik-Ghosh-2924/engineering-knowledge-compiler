package ekc.workspace.change;

import java.util.List;

public record RiskSignal(
        String category,
        String severity,
        String title,
        String description,
        List<String> evidence) {
    public RiskSignal {
        evidence = List.copyOf(evidence);
    }
}
