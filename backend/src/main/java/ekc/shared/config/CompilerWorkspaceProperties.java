package ekc.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the compiler workspace.
 * These properties define the filesystem layout used by the
 * Engineering Knowledge Compiler.
 */
@ConfigurationProperties(prefix = "compiler.workspace")
public class CompilerWorkspaceProperties {

    /**
     * Root workspace directory.
     * Repository storage directory.
     * Temporary working directory.
     */
    private String root;
    private String repositories;
    private String temporary;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getRepositories() {
        return repositories;
    }

    public void setRepositories(String repositories) {
        this.repositories = repositories;
    }

    public String getTemporary() {
        return temporary;
    }

    public void setTemporary(String temporary) {
        this.temporary = temporary;
    }
}