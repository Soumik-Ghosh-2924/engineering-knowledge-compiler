package ekc.api.response;

/**
 * REST response returned after a compilation request.
 */
public class CompileResponse {
    private final String status;
    private final String message;
    public CompileResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
    public String getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }
}
