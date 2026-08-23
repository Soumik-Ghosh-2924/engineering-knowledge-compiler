package ekc.api.request;

import jakarta.validation.constraints.NotBlank;

/**
 * REST request for initiating repository compilation.
 **/
public class CompileRequest {
    @NotBlank(message = "Repository URL must not be blank.")
    private String repositoryUrl;
    public CompileRequest() {
    }
    public String getRepositoryUrl() {
        return repositoryUrl;
    }
    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }
}