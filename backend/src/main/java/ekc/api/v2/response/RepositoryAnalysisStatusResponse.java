package ekc.api.v2.response;

import ekc.workspace.analysis.RepositoryAnalysisResult;

import java.util.UUID;

public record RepositoryAnalysisStatusResponse(
        UUID repositoryId,
        String repositoryUrl,
        String status,
        String message) {
    static RepositoryAnalysisStatusResponse from(RepositoryAnalysisResult result) {
        return new RepositoryAnalysisStatusResponse(
                result.repositoryId(), result.repositoryUrl(), result.status(), result.message());
    }
}
