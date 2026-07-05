package ekc.shared.exception;
public class RepositoryAcquisitionException extends RuntimeException {

    public RepositoryAcquisitionException(String message) {
        super(message);
    }

    public RepositoryAcquisitionException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}