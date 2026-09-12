package ekc.workspace.change;

import java.util.List;

public record RepositoryChangeAnalysis(
        ChangeAnalysisStatus status,
        String baseRef,
        String headRef,
        String baseCommit,
        String headCommit,
        int commitCount,
        boolean commitsTruncated,
        int changedFileCount,
        int additions,
        int deletions,
        List<CommitSummary> commits,
        List<ChangedFile> changedFiles,
        List<RiskSignal> riskSignals,
        String message) {

    public RepositoryChangeAnalysis {
        commits = List.copyOf(commits);
        changedFiles = List.copyOf(changedFiles);
        riskSignals = List.copyOf(riskSignals);
    }

    public static RepositoryChangeAnalysis notRequested() {
        return new RepositoryChangeAnalysis(
                ChangeAnalysisStatus.NOT_REQUESTED,
                null, null, null, null,
                0, false, 0, 0, 0,
                List.of(), List.of(), List.of(),
                "Provide both base and head refs to compare repository changes.");
    }

    public static RepositoryChangeAnalysis failed(String baseRef, String headRef, String message) {
        return new RepositoryChangeAnalysis(
                ChangeAnalysisStatus.FAILED,
                baseRef, headRef, null, null,
                0, false, 0, 0, 0,
                List.of(), List.of(), List.of(), message);
    }
}
