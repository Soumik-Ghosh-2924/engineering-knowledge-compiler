package ekc.shared.exception;

/**
 * Exception thrown when source discovery
 * fails during compiler execution.
 */
public class SourceDiscoveryException extends RuntimeException {
    public SourceDiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}