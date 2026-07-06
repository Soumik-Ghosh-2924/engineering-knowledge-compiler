package ekc.compiler.source;

import ekc.shared.exception.SourceDiscoveryException;
import ekc.shared.model.acquisition.CompilerContext;
import ekc.shared.model.acquisition.RepositoryMetadata;
import ekc.shared.model.source.RepositorySource;
import ekc.shared.model.source.SourceFile;
import ekc.shared.model.source.SourceLanguage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class SourceDiscoveryServiceImpl implements SourceDiscoveryService {

    private final SourceDiscoveryPolicy sourceDiscoveryPolicy;

    public SourceDiscoveryServiceImpl(SourceDiscoveryPolicy sourceDiscoveryPolicy) {
        this.sourceDiscoveryPolicy = sourceDiscoveryPolicy;
    }

    @Override
    public CompilerContext discover(CompilerContext context) {

        RepositoryMetadata repositoryMetadata = context.getRepositoryMetadata();
        Path repositoryRoot = repositoryMetadata.getLocalPath();
        List<SourceFile> sourceFiles = discoverSourceFiles(repositoryRoot);
        Map<SourceLanguage, Integer> languageDistribution = calculateLanguageDistribution(sourceFiles);
        RepositorySource repositorySource = new RepositorySource(sourceFiles, languageDistribution);

        return new CompilerContext(repositoryMetadata, repositorySource);
    }

    /**
     * Discovers all supported source files in the repository.
     */
    private List<SourceFile> discoverSourceFiles(Path repositoryRoot) {
        List<SourceFile> sourceFiles = new ArrayList<>();
        try {
            Files.walkFileTree(repositoryRoot, new SimpleFileVisitor<>() {

                        @Override
                        public FileVisitResult preVisitDirectory(Path directory,
                                BasicFileAttributes attributes) {
                            if (!sourceDiscoveryPolicy.shouldTraverse(directory)
                                    && !directory.equals(repositoryRoot)) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file,
                                BasicFileAttributes attributes) {
                            if (sourceDiscoveryPolicy.isSupportedSource(file)) {
                                sourceFiles.add(createSourceFile(repositoryRoot, file));
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });

        } catch (IOException exception) {
            throw new SourceDiscoveryException("Failed to discover source files from repository: " + repositoryRoot, exception);
        }
        return List.copyOf(sourceFiles);
    }

    /**
     * Creates the compiler representation of a source file.
     */
    private SourceFile createSourceFile(
            Path repositoryRoot,
            Path sourceFile) {

        String relativePath = repositoryRoot
                        .relativize(sourceFile)
                        .toString()
                        .replace("\\", "/");

        return new SourceFile(sourceFile, relativePath, SourceLanguage.JAVA
        );
    }


    private Map<SourceLanguage, Integer> calculateLanguageDistribution(List<SourceFile> sourceFiles) {

        Map<SourceLanguage, Integer> distribution = new EnumMap<>(SourceLanguage.class);
        for (SourceFile sourceFile : sourceFiles) {

            distribution.merge(
                    sourceFile.getLanguage(),
                    1,
                    Integer::sum
            );
        }
        return Map.copyOf(distribution);
    }
}