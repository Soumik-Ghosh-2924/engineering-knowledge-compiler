package ekc.shared.exception;

/**
 * Exception thrown when AST extraction fails.
 */
public class AstExtractionException extends RuntimeException {
    public AstExtractionException(String message) {
        super(message);
    }

    public AstExtractionException(String message, Throwable cause) {
        super(message, cause);
    }

}