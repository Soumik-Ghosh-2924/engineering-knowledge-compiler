package ekc.api.response;

import ekc.shared.model.analysis.CompilationResult;

/**
 * REST response returned after a repository analysis request.
 */
public class CompileResponse {
    private final String status;
    private final String message;
    private final Long durationMs;
    private final AnalysisSummaryResponse analysis;

    public CompileResponse(String status, String message) {
        this(status, message, null, null);
    }

    public CompileResponse(CompilationResult result) {
        this(
                result.getStatus(),
                result.getMessage(),
                result.getDurationMs(),
                new AnalysisSummaryResponse(result.getSummary()));
    }

    private CompileResponse(
            String status,
            String message,
            Long durationMs,
            AnalysisSummaryResponse analysis) {
        this.status = status;
        this.message = message;
        this.durationMs = durationMs;
        this.analysis = analysis;
    }
    public String getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public AnalysisSummaryResponse getAnalysis() {
        return analysis;
    }
}
