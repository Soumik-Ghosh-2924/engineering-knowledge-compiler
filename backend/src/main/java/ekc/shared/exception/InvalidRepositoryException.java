package ekc.shared.exception;

/**
 * Thrown when a repository request fails validation.
 */
public class InvalidRepositoryException extends RuntimeException {

    public InvalidRepositoryException(String message) {
        super(message);
    }

    public InvalidRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}