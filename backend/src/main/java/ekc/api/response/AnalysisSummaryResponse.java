package ekc.api.response;

import ekc.shared.model.analysis.CompilationSummary;

import java.util.List;

public class AnalysisSummaryResponse {

    private final String repositoryName;
    private final String defaultBranch;
    private final int sourceFiles;
    private final int parsedFiles;
    private final int extractedFiles;
    private final int packages;
    private final int imports;
    private final int types;
    private final int fields;
    private final int methods;
    private final int annotations;
    private final List<AnalysisDiagnosticResponse> diagnostics;

    public AnalysisSummaryResponse(CompilationSummary summary) {
        this.repositoryName = summary.getRepositoryName();
        this.defaultBranch = summary.getDefaultBranch();
        this.sourceFiles = summary.getSourceFiles();
        this.parsedFiles = summary.getParsedFiles();
        this.extractedFiles = summary.getExtractedFiles();
        this.packages = summary.getPackages();
        this.imports = summary.getImports();
        this.types = summary.getTypes();
        this.fields = summary.getFields();
        this.methods = summary.getMethods();
        this.annotations = summary.getAnnotations();
        this.diagnostics = summary.getDiagnostics().stream()
                .map(AnalysisDiagnosticResponse::new)
                .toList();
    }

    public String getRepositoryName() { return repositoryName; }
    public String getDefaultBranch() { return defaultBranch; }
    public int getSourceFiles() { return sourceFiles; }
    public int getParsedFiles() { return parsedFiles; }
    public int getExtractedFiles() { return extractedFiles; }
    public int getPackages() { return packages; }
    public int getImports() { return imports; }
    public int getTypes() { return types; }
    public int getFields() { return fields; }
    public int getMethods() { return methods; }
    public int getAnnotations() { return annotations; }
    public List<AnalysisDiagnosticResponse> getDiagnostics() { return diagnostics; }
}
