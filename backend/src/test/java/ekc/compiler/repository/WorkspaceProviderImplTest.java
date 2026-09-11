package ekc.compiler.repository;

import ekc.shared.config.CompilerWorkspaceProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceProviderImplTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void isolatesRepositoriesWithTheSameNameUnderDifferentOwners() {
        CompilerWorkspaceProperties properties = new CompilerWorkspaceProperties();
        properties.setRoot(temporaryDirectory.toString());
        properties.setRepositories("repositories");
        properties.setTemporary("temporary");
        WorkspaceProviderImpl provider = new WorkspaceProviderImpl(properties);

        Path first = provider.resolveRepositoryLocation(URI.create("https://github.com/owner-a/platform.git"));
        Path second = provider.resolveRepositoryLocation(URI.create("https://github.com/owner-b/platform.git"));

        assertThat(first).isNotEqualTo(second);
        assertThat(first.toString()).endsWith("github.com/owner-a/platform");
        assertThat(second.toString()).endsWith("github.com/owner-b/platform");
    }
}
