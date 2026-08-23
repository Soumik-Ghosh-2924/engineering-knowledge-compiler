package ekc.shared.exception;

/**
 * Exception thrown when AST parsing fails.
 */
public class AstParserException
        extends RuntimeException {

    public AstParserException(String message) {
        super(message);
    }

    public AstParserException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}