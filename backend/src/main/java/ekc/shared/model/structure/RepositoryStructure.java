package ekc.shared.model.structure;

import java.util.List;
import java.util.Objects;

/**
 * Represents the compiler's extracted structural model
 * of an entire repository.
 *
 * This is the output of Stage 2.3 (AST Extraction).
 */
public final class RepositoryStructure {

    private final List<ExtractedSourceFile> sourceFiles;

    public RepositoryStructure(
            List<ExtractedSourceFile> sourceFiles) {

        this.sourceFiles =
                List.copyOf(
                        Objects.requireNonNull(sourceFiles));
    }

    public List<ExtractedSourceFile> getSourceFiles() {
        return sourceFiles;
    }

    @Override
    public String toString() {
        return "RepositoryStructure{" +
                "sourceFiles=" + sourceFiles.size() +
                '}';
    }
}