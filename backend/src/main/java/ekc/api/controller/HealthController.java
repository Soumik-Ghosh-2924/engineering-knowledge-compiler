package ekc.api.controller;

import ekc.api.response.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health endpoint for verifying compiler availability.
 */
@RestController
public class HealthController {
    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP");
    }
}