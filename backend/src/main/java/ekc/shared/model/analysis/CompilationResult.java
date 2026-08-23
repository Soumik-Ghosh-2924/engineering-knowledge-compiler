package ekc.shared.model.analysis;

import java.util.Objects;

/**
 * Final result returned by the compiler orchestration layer.
 */
public final class CompilationResult {

    private final String status;
    private final String message;
    private final long durationMs;
    private final CompilationSummary summary;

    public CompilationResult(
            String status,
            String message,
            long durationMs,
            CompilationSummary summary) {

        this.status = Objects.requireNonNull(status);
        this.message = Objects.requireNonNull(message);
        this.durationMs = durationMs;
        this.summary = Objects.requireNonNull(summary);
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public long getDurationMs() { return durationMs; }
    public CompilationSummary getSummary() { return summary; }
}
