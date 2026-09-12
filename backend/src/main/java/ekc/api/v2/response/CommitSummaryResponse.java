package ekc.api.v2.response;

import ekc.workspace.change.CommitSummary;

import java.time.Instant;

public record CommitSummaryResponse(
        String id,
        String shortId,
        String message,
        String author,
        Instant authoredAt) {
    static CommitSummaryResponse from(CommitSummary commit) {
        return new CommitSummaryResponse(
                commit.id(), commit.shortId(), commit.message(), commit.author(), commit.authoredAt());
    }
}
