package ekc.shared.model.source;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents all source files discovered within a repository.
 * This model is the output of the Source Discovery stage and
 * serves as the compiler's initial understanding of the
 * repository's source tree. This model is going to immutable so
 * that at any stage the compiler's state is not mutated/changed manually.
 */
public final class RepositorySource {

    private final List<SourceFile> sourceFiles;
    private final int totalSourceFiles;
    private final Map<SourceLanguage, Integer> languageDistribution;

    public RepositorySource(List<SourceFile> sourceFiles, Map<SourceLanguage, Integer> languageDistribution) {
        Objects.requireNonNull(sourceFiles, "Source files cannot be null.");
        Objects.requireNonNull(languageDistribution, "Language distribution cannot be null.");
        this.sourceFiles = List.copyOf(sourceFiles);
        this.totalSourceFiles = sourceFiles.size();
        this.languageDistribution = Map.copyOf(languageDistribution);
    }

    public List<SourceFile> getSourceFiles() {
        return Collections.unmodifiableList(sourceFiles);
    }

    public int getTotalSourceFiles() {
        return totalSourceFiles;
    }

    public Map<SourceLanguage, Integer> getLanguageDistribution() {
        return Collections.unmodifiableMap(languageDistribution);
    }
}