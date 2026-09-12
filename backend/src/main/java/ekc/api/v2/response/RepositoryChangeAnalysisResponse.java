package ekc.api.v2.response;

import ekc.workspace.change.RepositoryChangeAnalysis;

import java.util.List;

public record RepositoryChangeAnalysisResponse(
        String status,
        String baseRef,
        String headRef,
        String baseCommit,
        String headCommit,
        int commitCount,
        boolean commitsTruncated,
        int changedFileCount,
        int additions,
        int deletions,
        List<CommitSummaryResponse> commits,
        List<ChangedFileResponse> changedFiles,
        List<RiskSignalResponse> riskSignals,
        String message) {

    public static RepositoryChangeAnalysisResponse from(RepositoryChangeAnalysis analysis) {
        if (analysis == null) return null;
        return new RepositoryChangeAnalysisResponse(
                analysis.status().name(),
                analysis.baseRef(),
                analysis.headRef(),
                analysis.baseCommit(),
                analysis.headCommit(),
                analysis.commitCount(),
                analysis.commitsTruncated(),
                analysis.changedFileCount(),
                analysis.additions(),
                analysis.deletions(),
                analysis.commits().stream().map(CommitSummaryResponse::from).toList(),
                analysis.changedFiles().stream().map(ChangedFileResponse::from).toList(),
                analysis.riskSignals().stream().map(RiskSignalResponse::from).toList(),
                analysis.message());
    }
}
