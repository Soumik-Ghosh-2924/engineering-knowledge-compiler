package ekc.compiler.source;

import ekc.shared.model.source.SourceLanguage;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Default implementation of the compiler's source
 * discovery policy.Responsible for deciding which
 * directories are to be traversed and which files
 * are considered source files for compilation.
 */
@Component
public class SourceDiscoveryPolicyImpl implements SourceDiscoveryPolicy {

    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            ".git",
            ".idea",
            ".gradle",
            "target",
            "build",
            "out",
            "node_modules"
    );

    @Override
    public boolean shouldTraverse(Path directory) {
        if (!Files.isDirectory(directory)) {
            return false;
        }
        String directoryName =directory.getFileName().toString();
        return !IGNORED_DIRECTORIES.contains(directoryName);
    }

    /**
     * Based on the compiler's current capability
     * it's going to validate any specific file that
     * the compiler gets whether it's of a type that it
     * itself supports.
     */
    @Override
    public boolean isSupportedSource(Path file) {
        if (!Files.isRegularFile(file)) {
            return false;
        }
        String fileName = file.getFileName().toString();
        return fileName.endsWith(SourceLanguage.JAVA.getExtension());
    }
}