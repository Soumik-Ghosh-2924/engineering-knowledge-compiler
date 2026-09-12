package ekc.workspace.change;

public record ChangedFile(
        String path,
        String changeType,
        int additions,
        int deletions) {
}
