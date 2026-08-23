package ekc.shared.exception;

public class WorkspaceInitializationException extends RuntimeException {

    public WorkspaceInitializationException(String message) {
        super(message);
    }

    public WorkspaceInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
