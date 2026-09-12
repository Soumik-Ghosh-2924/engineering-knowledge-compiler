package ekc.workspace.change;

import java.time.Instant;

public record CommitSummary(
        String id,
        String shortId,
        String message,
        String author,
        Instant authoredAt) {
}
