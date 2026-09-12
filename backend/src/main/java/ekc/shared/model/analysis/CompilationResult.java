package ekc.shared.model.analysis;

import ekc.shared.model.graph.RepositoryKnowledgeGraph;

import java.util.Objects;

/**
 * Final result returned by the compiler orchestration layer.
 */
public final class CompilationResult {

    private final String status;
    private final String message;
    private final long durationMs;
    private final CompilationSummary summary;
    private final RepositoryKnowledgeGraph knowledgeGraph;

    public CompilationResult(
            String status,
            String message,
            long durationMs,
            CompilationSummary summary) {

        this(status, message, durationMs, summary, RepositoryKnowledgeGraph.empty());
    }

    public CompilationResult(
            String status,
            String message,
            long durationMs,
            CompilationSummary summary,
            RepositoryKnowledgeGraph knowledgeGraph) {

        this.status = Objects.requireNonNull(status);
        this.message = Objects.requireNonNull(message);
        this.durationMs = durationMs;
        this.summary = Objects.requireNonNull(summary);
        this.knowledgeGraph = Objects.requireNonNull(knowledgeGraph);
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public long getDurationMs() { return durationMs; }
    public CompilationSummary getSummary() { return summary; }
    public RepositoryKnowledgeGraph getKnowledgeGraph() { return knowledgeGraph; }
}
