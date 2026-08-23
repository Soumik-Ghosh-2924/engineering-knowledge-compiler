package ekc.api.response;

import ekc.shared.model.analysis.CompilationDiagnostic;

public class AnalysisDiagnosticResponse {

    private final String stage;
    private final String severity;
    private final String sourcePath;
    private final String message;

    public AnalysisDiagnosticResponse(CompilationDiagnostic diagnostic) {
        this.stage = diagnostic.getStage().name();
        this.severity = diagnostic.getSeverity().name();
        this.sourcePath = diagnostic.getSourcePath();
        this.message = diagnostic.getMessage();
    }

    public String getStage() { return stage; }
    public String getSeverity() { return severity; }
    public String getSourcePath() { return sourcePath; }
    public String getMessage() { return message; }
}
