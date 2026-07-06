package ekc.shared.model.source;

/**
 * Represents a programming language supported by the
 * Engineering Knowledge Compiler.
 * Every discovered source file is associated with one
 * of these languages, allowing future compiler stages
 * to delegate parsing to the appropriate language parser.
 */
public enum SourceLanguage {

    /**
     * Java source code.
     */
    JAVA(".java");

    private final String extension;

    SourceLanguage(String extension) {
        this.extension = extension;
    }

    /**
     * Returns the standard file extension associated
     * with this programming language.
     * @return source file extension
     */
    public String getExtension() {
        return extension;
    }
}