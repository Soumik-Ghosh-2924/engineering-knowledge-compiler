package ekc.shared.model.source;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a source file discovered by the compiler.
 * A SourceFile contains only the information required to
 * uniquely identify and locate a source file within a
 * repository. It deliberately contains no parsed or
 * semantic information.
 */
public final class SourceFile {

    // Absolute path of the source file on the local filesystem.
    private final Path path;

    // Example: src/main/java/com/ekc/api/CompileController.java
    private final String relativePath;
    private final SourceLanguage language;

    public SourceFile(Path path, String relativePath, SourceLanguage language) {
        this.path = Objects.requireNonNull(path, "Source file path cannot be null.");
        this.relativePath = Objects.requireNonNull(relativePath, "Relative path cannot be null.");
        this.language = Objects.requireNonNull(language, "Source language cannot be null.");
    }


    public Path getPath() {
        return path;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public SourceLanguage getLanguage() {
        return language;
    }
}