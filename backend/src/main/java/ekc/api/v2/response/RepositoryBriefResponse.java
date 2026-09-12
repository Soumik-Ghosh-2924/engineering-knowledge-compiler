package ekc.api.v2.response;

import ekc.api.response.AnalysisSummaryResponse;
import ekc.workspace.model.RepositoryRole;

import java.util.UUID;

public record RepositoryBriefResponse(
        UUID repositoryId,
        String repositoryUrl,
        RepositoryRole role,
        boolean primary,
        String status,
        PurposeStatementResponse purpose,
        AnalysisSummaryResponse analysis,
        RepositoryChangeAnalysisResponse changeAnalysis,
        String message) {
}
