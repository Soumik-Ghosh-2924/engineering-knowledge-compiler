package ekc.workspace.analysis;

import ekc.api.v2.response.EvidenceCitationResponse;
import ekc.api.v2.response.PurposeStatementResponse;
import ekc.compiler.repository.WorkspaceProvider;
import ekc.shared.model.analysis.CompilationSummary;
import ekc.workspace.model.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class RepositoryUnderstandingService {
    private static final int MAX_README_LINES = 240;
    private static final int MAX_SUMMARY_LENGTH = 700;
    private static final Pattern MARKDOWN_LINK = Pattern.compile("\\[([^]]+)]\\([^)]*\\)");
    private final WorkspaceProvider workspaceProvider;

    public RepositoryUnderstandingService(WorkspaceProvider workspaceProvider) {
        this.workspaceProvider = workspaceProvider;
    }

    public PurposeStatementResponse understand(
            WorkspaceRepository repository,
            CompilationSummary summary) {
        Path repositoryPath = workspaceProvider.resolveRepositoryLocation(repository.repositoryUri());
        Optional<Path> readme = findReadme(repositoryPath);
        if (readme.isPresent()) {
            String description = readReadmeSummary(readme.get(), summary.getRepositoryName());
            if (!description.isBlank()) {
                return inferred(
                        description,
                        "HIGH",
                        "README",
                        repositoryPath.relativize(readme.get()).toString(),
                        "Repository understanding extracted from the strongest descriptive README section.");
            }
        }
        return inferred(
                codeSummary(repository, summary),
                "LOW",
                "STATIC_ANALYSIS",
                repository.repositoryUri().toString(),
                "Repository understanding inferred from role and structural code metrics.");
    }

    private PurposeStatementResponse inferred(
            String value,
            String confidence,
            String sourceType,
            String source,
            String description) {
        return new PurposeStatementResponse(
                limit(value),
                "INFERRED",
                confidence,
                List.of(new EvidenceCitationResponse(sourceType, source, description)));
    }

    private Optional<Path> findReadme(Path repositoryPath) {
        try (Stream<Path> files = Files.list(repositoryPath)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(Locale.ROOT)
                            .matches("^readme(?:\\.(?:md|markdown|adoc|rst|txt))?$"))
                    .sorted(Comparator.comparingInt(this::readmePreference))
                    .findFirst();
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private int readmePreference(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".md") || name.endsWith(".markdown") ? 0 : 1;
    }

    private String readReadmeSummary(Path readme, String repositoryName) {
        try (BufferedReader reader = Files.newBufferedReader(readme, StandardCharsets.UTF_8)) {
            List<String> paragraphs = new ArrayList<>();
            StringBuilder paragraph = new StringBuilder();
            boolean inCodeBlock = false;
            int lines = 0;
            String line;
            while ((line = reader.readLine()) != null && lines++ < MAX_README_LINES) {
                String trimmed = line.trim();
                if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                    addParagraph(paragraphs, paragraph);
                    inCodeBlock = !inCodeBlock;
                    continue;
                }
                if (inCodeBlock) continue;
                if (trimmed.isBlank() || trimmed.startsWith("#")) {
                    addParagraph(paragraphs, paragraph);
                    continue;
                }
                if (isDecoration(trimmed)) continue;
                String plain = plainText(trimmed);
                if (plain.isBlank()) continue;
                if (!paragraph.isEmpty()) paragraph.append(' ');
                paragraph.append(plain);
                if (paragraph.length() >= MAX_SUMMARY_LENGTH) {
                    addParagraph(paragraphs, paragraph);
                }
            }
            addParagraph(paragraphs, paragraph);
            return bestParagraph(paragraphs, repositoryName);
        } catch (IOException exception) {
            return "";
        }
    }

    private void addParagraph(List<String> paragraphs, StringBuilder paragraph) {
        if (!paragraph.isEmpty()) paragraphs.add(limit(paragraph.toString()));
        paragraph.setLength(0);
    }

    private String bestParagraph(List<String> paragraphs, String repositoryName) {
        String best = "";
        int bestScore = Integer.MIN_VALUE;
        for (String candidate : paragraphs) {
            int score = paragraphScore(candidate, repositoryName);
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return bestScore > 0 ? best : "";
    }

    private int paragraphScore(String paragraph, String repositoryName) {
        String value = paragraph.toLowerCase(Locale.ROOT);
        int score = paragraph.length() >= 60 ? 3 : paragraph.length() >= 30 ? 1 : -2;
        if (value.matches(".*\\b(is an?|provides?|enables?|helps?|allows?|offers?|implements?|manages?|supports?|coordinates?|serves?)\\b.*")) {
            score += 6;
        }
        String repositoryToken = repositoryName.toLowerCase(Locale.ROOT).replace('-', ' ');
        if (value.contains(repositoryToken) || value.contains(repositoryName.toLowerCase(Locale.ROOT))) score += 3;
        if (value.matches("^(see|click|read|note:|for more|documentation|learn more).*")) score -= 10;
        if (value.contains("license") || value.contains("contribut") || value.contains("issue tracker")) score -= 5;
        return score;
    }

    private boolean isDecoration(String line) {
        return line.startsWith("![")
                || line.startsWith("[![")
                || line.matches("^<[^>]+>$")
                || line.matches("^[-=*_| ]{3,}$");
    }

    private String plainText(String markdown) {
        String value = MARKDOWN_LINK.matcher(markdown).replaceAll("$1");
        return value
                .replaceFirst("^[-*+]\\s+", "")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("[`*_~]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String codeSummary(
            WorkspaceRepository repository,
            CompilationSummary summary) {
        String role = repository.role().name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return "A " + role + " repository containing "
                + summary.getSourceFiles() + " Java source files across "
                + summary.getPackages() + " packages, defining "
                + summary.getTypes() + " types and "
                + summary.getMethods() + " methods. Its business consumers are not stated "
                + "in available repository documentation.";
    }

    private String limit(String value) {
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= MAX_SUMMARY_LENGTH
                ? normalized
                : normalized.substring(0, MAX_SUMMARY_LENGTH - 1).trim() + "…";
    }
}
