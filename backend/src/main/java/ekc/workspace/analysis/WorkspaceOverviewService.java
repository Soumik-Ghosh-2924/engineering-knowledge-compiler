package ekc.workspace.analysis;

import ekc.api.response.AnalysisSummaryResponse;
import ekc.api.v2.response.EvidenceCitationResponse;
import ekc.api.v2.response.PurposeStatementResponse;
import ekc.api.v2.response.RepositoryBriefResponse;
import ekc.api.v2.response.RepositoryChangeAnalysisResponse;
import ekc.api.v2.response.RepositoryRelationshipResponse;
import ekc.api.v2.response.SystemOverviewResponse;
import ekc.api.v2.response.WorkspaceOverviewResponse;
import ekc.workspace.WorkspaceService;
import ekc.workspace.model.RepositoryRole;
import ekc.workspace.model.Workspace;
import ekc.workspace.model.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WorkspaceOverviewService {
    private final WorkspaceService workspaceService;
    private final WorkspaceAnalysisService analysisService;
    private final RepositoryUnderstandingService understandingService;

    public WorkspaceOverviewService(
            WorkspaceService workspaceService,
            WorkspaceAnalysisService analysisService,
            RepositoryUnderstandingService understandingService) {
        this.workspaceService = workspaceService;
        this.analysisService = analysisService;
        this.understandingService = understandingService;
    }

    public WorkspaceOverviewResponse create(UUID analysisId) {
        WorkspaceAnalysis analysis = analysisService.get(analysisId);
        if (analysis.status() == WorkspaceAnalysisStatus.QUEUED
                || analysis.status() == WorkspaceAnalysisStatus.RUNNING) {
            throw new IllegalStateException("Workspace analysis is not complete.");
        }
        Workspace workspace = workspaceService.get(analysis.workspaceId());
        List<RepositoryBriefResponse> briefs = workspace.repositories().stream()
                .map(repository -> createBrief(repository, analysis))
                .toList();
        int succeeded = (int) analysis.repositories().stream()
                .filter(result -> result.compilationResult() != null)
                .count();
        return new WorkspaceOverviewResponse(
                analysis.id(),
                workspace.id(),
                workspace.name(),
                analysis.status().name(),
                briefs,
                new SystemOverviewResponse(
                        workspace.repositories().size(),
                        succeeded,
                        workspace.repositories().size() - succeeded,
                        suggestRelationships(workspace)));
    }

    private RepositoryBriefResponse createBrief(
            WorkspaceRepository repository,
            WorkspaceAnalysis analysis) {
        RepositoryAnalysisResult result = analysis.repositories().stream()
                .filter(candidate -> candidate.repositoryId().equals(repository.id()))
                .findFirst()
                .orElse(new RepositoryAnalysisResult(
                        repository.id(), repository.repositoryUri().toString(),
                        "FAILED", "Repository was not analyzed.", null, null));
        AnalysisSummaryResponse summary = result.compilationResult() == null
                ? null
                : new AnalysisSummaryResponse(result.compilationResult().getSummary());
        return new RepositoryBriefResponse(
                repository.id(),
                repository.repositoryUri().toString(),
                repository.role(),
                repository.primary(),
                result.status(),
                result.compilationResult() == null
                        ? new PurposeStatementResponse(null, "UNKNOWN", "NONE", List.of())
                        : understandingService.understand(
                                repository, result.compilationResult().getSummary()),
                summary,
                RepositoryChangeAnalysisResponse.from(result.changeAnalysis()),
                result.message());
    }

    private List<RepositoryRelationshipResponse> suggestRelationships(Workspace workspace) {
        List<RepositoryRelationshipResponse> suggestions = new ArrayList<>();
        List<WorkspaceRepository> applications = workspace.repositories().stream()
                .filter(repository -> repository.role() == RepositoryRole.APPLICATION)
                .toList();
        List<WorkspaceRepository> deployments = workspace.repositories().stream()
                .filter(repository -> repository.role() == RepositoryRole.DEPLOYMENT)
                .toList();
        for (WorkspaceRepository application : applications) {
            for (WorkspaceRepository deployment : deployments) {
                suggestions.add(new RepositoryRelationshipResponse(
                        application.id(),
                        deployment.id(),
                        "MAY_BE_DEPLOYED_BY",
                        "LOW",
                        false,
                        List.of(new EvidenceCitationResponse(
                                "WORKSPACE_ROLE",
                                deployment.repositoryUri().toString(),
                                "Suggested from user-assigned roles; source evidence is not yet confirmed."))));
            }
        }
        return List.copyOf(suggestions);
    }
}
