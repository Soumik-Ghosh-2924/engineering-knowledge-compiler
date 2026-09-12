package ekc.workspace.analysis;

import ekc.shared.model.analysis.CompilationResult;
import ekc.workspace.change.RepositoryChangeAnalysis;

import java.util.UUID;

public record RepositoryAnalysisResult(
        UUID repositoryId,
        String repositoryUrl,
        String status,
        String message,
        CompilationResult compilationResult,
        RepositoryChangeAnalysis changeAnalysis) {
}
