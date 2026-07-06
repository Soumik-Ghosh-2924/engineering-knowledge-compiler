package ekc.compiler.source;

import java.nio.file.Path;

/**
 * Defines the policy used by the compiler to determine
 * whether a filesystem path should be traversed or
 * considered a supported source file.
 */
public interface SourceDiscoveryPolicy {

    /**
     * Determines whether the supplied directory should
     * be traversed during source discovery.
     * @param directory directory path
     * @return true if traversal should continue
     */
    boolean shouldTraverse(Path directory);

    /**
     * Determines whether the supplied file should be
     * included as a compiler source file.
     * @param file source file candidate
     * @return true if supported by the compiler
     */
    boolean isSupportedSource(Path file);
}