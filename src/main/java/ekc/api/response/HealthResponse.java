package ekc.api.response;

/**
 * Health endpoint response.
 */
public class HealthResponse {
    private final String status;
    public HealthResponse(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
}