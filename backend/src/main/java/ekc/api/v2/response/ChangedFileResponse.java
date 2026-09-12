package ekc.api.v2.response;

import ekc.workspace.change.ChangedFile;

public record ChangedFileResponse(
        String path,
        String changeType,
        int additions,
        int deletions,
        String diffUrl) {
    static ChangedFileResponse from(ChangedFile file) {
        return new ChangedFileResponse(
                file.path(), file.changeType(), file.additions(), file.deletions(), file.diffUrl());
    }
}
