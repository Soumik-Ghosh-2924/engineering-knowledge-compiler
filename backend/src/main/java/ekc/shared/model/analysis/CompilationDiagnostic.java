package ekc.shared.model.analysis;

import java.util.Objects;

/**
 * A non-fatal problem recorded while analyzing a repository.
 */
public final class CompilationDiagnostic {

    private final AnalysisStage stage;
    private final DiagnosticSeverity severity;
    private final String sourcePath;
    private final String message;

    public CompilationDiagnostic(
            AnalysisStage stage,
            DiagnosticSeverity severity,
            String sourcePath,
            String message) {

        this.stage = Objects.requireNonNull(stage);
        this.severity = Objects.requireNonNull(severity);
        this.sourcePath = sourcePath;
        this.message = Objects.requireNonNull(message);
    }

    public AnalysisStage getStage() {
        return stage;
    }

    public DiagnosticSeverity getSeverity() {
        return severity;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getMessage() {
        return message;
    }
}
