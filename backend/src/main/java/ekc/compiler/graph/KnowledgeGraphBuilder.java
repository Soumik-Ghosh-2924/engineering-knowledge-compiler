package ekc.compiler.graph;

import ekc.shared.model.acquisition.CompilerContext;
import ekc.shared.model.ast.ParsedField;
import ekc.shared.model.ast.ParsedMethod;
import ekc.shared.model.ast.ParsedType;
import ekc.shared.model.ast.ParsedVariable;
import ekc.shared.model.ast.TypeKind;
import ekc.shared.model.graph.KnowledgeEdge;
import ekc.shared.model.graph.KnowledgeField;
import ekc.shared.model.graph.KnowledgeMethod;
import ekc.shared.model.graph.KnowledgeNode;
import ekc.shared.model.graph.KnowledgeVariable;
import ekc.shared.model.graph.RepositoryKnowledgeGraph;
import ekc.shared.model.structure.ExtractedSourceFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KnowledgeGraphBuilder {
    private static final int MAX_TYPES = 200;
    private static final Pattern TYPE_TOKEN = Pattern.compile("[A-Za-z_$][\\w$]*(?:\\.[A-Za-z_$][\\w$]*)*");
    private static final Set<String> COMPONENT_KINDS = Set.of("CONTROLLER", "SERVICE", "REPOSITORY");

    public RepositoryKnowledgeGraph build(CompilerContext context) {
        List<TypeContext> allTypes = collectTypes(context);
        List<TypeContext> included = allTypes.stream()
                .sorted(Comparator.comparingInt((TypeContext item) -> kindPriority(item.node().kind()))
                        .thenComparing(item -> item.node().qualifiedName()))
                .limit(MAX_TYPES)
                .toList();
        Map<String, TypeContext> byId = new LinkedHashMap<>();
        Map<String, String> bySimpleName = new HashMap<>();
        for (TypeContext item : included) {
            byId.put(item.node().id(), item);
            bySimpleName.putIfAbsent(item.type().getName(), item.node().id());
        }

        Map<String, KnowledgeEdge> edges = new LinkedHashMap<>();
        for (TypeContext item : included) {
            addInheritanceEdges(item, bySimpleName, edges);
            addMemberDependencyEdges(item, bySimpleName, edges);
            addImportEdges(item, bySimpleName, edges);
        }
        addFrameworkDiscoveryEdges(included, edges);

        return new RepositoryKnowledgeGraph(
                included.stream().map(TypeContext::node).toList(),
                edges.values().stream()
                        .filter(edge -> byId.containsKey(edge.source()) && byId.containsKey(edge.target()))
                        .toList(),
                allTypes.size(),
                allTypes.size() > MAX_TYPES);
    }

    private List<TypeContext> collectTypes(CompilerContext context) {
        List<TypeContext> items = new ArrayList<>();
        for (ExtractedSourceFile file : context.getRepositoryStructure().getSourceFiles()) {
            String packageName = file.getParsedCompilationUnit().getPackageName();
            String sourcePath = file.getParsedSourceFile().getSourceFile().getRelativePath();
            List<String> imports = file.getParsedCompilationUnit().getImports();
            for (ParsedType type : file.getParsedCompilationUnit().getTypes()) {
                String qualifiedName = packageName == null || packageName.isBlank()
                        ? type.getName()
                        : packageName + "." + type.getName();
                KnowledgeNode node = new KnowledgeNode(
                        "type:" + qualifiedName,
                        type.getName(),
                        classify(type),
                        qualifiedName,
                        packageName,
                        sourcePath,
                        type.getAnnotations(),
                        imports,
                        type.getFields().stream().map(field -> new KnowledgeField(field.getName(), field.getType())).toList(),
                        type.getMethods().stream().map(this::method).toList());
                items.add(new TypeContext(type, node));
            }
        }
        return items;
    }

    private KnowledgeMethod method(ParsedMethod method) {
        return new KnowledgeMethod(
                method.getName(),
                method.getReturnType(),
                method.getAnnotations(),
                method.getParameters().stream().map(this::variable).toList(),
                method.getLocalVariables().stream().map(this::variable).toList());
    }

    private KnowledgeVariable variable(ParsedVariable variable) {
        return new KnowledgeVariable(variable.name(), variable.type());
    }

    private String classify(ParsedType type) {
        Set<String> annotations = type.getAnnotations().stream()
                .map(this::simpleName)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
        String name = type.getName().toLowerCase(Locale.ROOT);
        List<String> hierarchy = new ArrayList<>(type.getExtendedTypes());
        hierarchy.addAll(type.getImplementedTypes());
        if (annotations.contains("springbootapplication")) return "APPLICATION";
        if (annotations.contains("restcontroller") || annotations.contains("controller")) return "CONTROLLER";
        if (annotations.contains("service")) return "SERVICE";
        if (annotations.contains("repository") || hierarchy.stream().map(this::simpleName)
                .anyMatch(value -> value.endsWith("Repository"))) return "REPOSITORY";
        if (annotations.stream().anyMatch(Set.of("entity", "table", "embeddable", "document")::contains)
                || name.matches(".*(?:dto|dao|model|entity|request|response)$")) return "MODEL";
        if (type.getKind() == TypeKind.INTERFACE) return "INTERFACE";
        return type.getKind().name();
    }

    private void addInheritanceEdges(
            TypeContext source,
            Map<String, String> bySimpleName,
            Map<String, KnowledgeEdge> edges) {
        source.type().getExtendedTypes().forEach(target -> addResolvedEdge(
                source.node().id(), target, "EXTENDS", "extends", "HIGH",
                "Declared extends clause: " + target, bySimpleName, edges));
        source.type().getImplementedTypes().forEach(target -> addResolvedEdge(
                source.node().id(), target, "IMPLEMENTS", "implements", "HIGH",
                "Declared implements clause: " + target, bySimpleName, edges));
    }

    private void addMemberDependencyEdges(
            TypeContext source,
            Map<String, String> bySimpleName,
            Map<String, KnowledgeEdge> edges) {
        for (ParsedField field : source.type().getFields()) {
            addTypeReferences(source.node().id(), field.getType(), "DEPENDS_ON", "field " + field.getName(),
                    "Field type: " + field.getType(), bySimpleName, edges);
        }
        for (ParsedMethod method : source.type().getMethods()) {
            addTypeReferences(source.node().id(), method.getReturnType(), "RETURNS", method.getName() + " returns",
                    "Method return type: " + method.getReturnType(), bySimpleName, edges);
            for (String parameterType : method.getParameterTypes()) {
                addTypeReferences(source.node().id(), parameterType, "ACCEPTS", method.getName() + " accepts",
                        "Method parameter type: " + parameterType, bySimpleName, edges);
            }
            for (ParsedVariable variable : method.getLocalVariables()) {
                addTypeReferences(source.node().id(), variable.type(), "USES", method.getName() + " uses",
                        "Local variable " + variable.name() + ": " + variable.type(), bySimpleName, edges);
            }
        }
    }

    private void addImportEdges(
            TypeContext source,
            Map<String, String> bySimpleName,
            Map<String, KnowledgeEdge> edges) {
        for (String imported : source.node().imports()) {
            String target = bySimpleName.get(simpleName(imported));
            if (target == null || target.equals(source.node().id())) continue;
            boolean hasStrongerEdge = edges.values().stream()
                    .anyMatch(edge -> edge.source().equals(source.node().id()) && edge.target().equals(target));
            if (!hasStrongerEdge) addEdge(new KnowledgeEdge(
                    source.node().id(), target, "IMPORTS", "imports", "HIGH",
                    "Import declaration: " + imported), edges);
        }
    }

    private void addFrameworkDiscoveryEdges(List<TypeContext> types, Map<String, KnowledgeEdge> edges) {
        List<TypeContext> applications = types.stream().filter(item -> item.node().kind().equals("APPLICATION")).toList();
        List<TypeContext> components = types.stream().filter(item -> COMPONENT_KINDS.contains(item.node().kind())).toList();
        for (TypeContext application : applications) {
            for (TypeContext component : components) {
                addEdge(new KnowledgeEdge(
                        application.node().id(), component.node().id(), "DISCOVERS", "framework discovery", "MEDIUM",
                        "Inferred from @SpringBootApplication component scanning and @" + component.node().kind()), edges);
            }
        }
    }

    private void addTypeReferences(
            String source,
            String typeExpression,
            String kind,
            String label,
            String evidence,
            Map<String, String> bySimpleName,
            Map<String, KnowledgeEdge> edges) {
        Matcher matcher = TYPE_TOKEN.matcher(typeExpression);
        Set<String> names = new LinkedHashSet<>();
        while (matcher.find()) names.add(simpleName(matcher.group()));
        for (String name : names) {
            addResolvedEdge(source, name, kind, label, "HIGH", evidence, bySimpleName, edges);
        }
    }

    private void addResolvedEdge(
            String source,
            String targetName,
            String kind,
            String label,
            String confidence,
            String evidence,
            Map<String, String> bySimpleName,
            Map<String, KnowledgeEdge> edges) {
        String target = bySimpleName.get(simpleName(targetName));
        if (target == null || target.equals(source)) return;
        addEdge(new KnowledgeEdge(source, target, kind, label, confidence, evidence), edges);
    }

    private void addEdge(KnowledgeEdge edge, Map<String, KnowledgeEdge> edges) {
        edges.putIfAbsent(edge.source() + "|" + edge.target() + "|" + edge.kind(), edge);
    }

    private String simpleName(String value) {
        String clean = value.replace("[]", "");
        int index = Math.max(clean.lastIndexOf('.'), clean.lastIndexOf('$'));
        return index >= 0 ? clean.substring(index + 1) : clean;
    }

    private int kindPriority(String kind) {
        return switch (kind) {
            case "APPLICATION" -> 0;
            case "CONTROLLER" -> 1;
            case "SERVICE" -> 2;
            case "REPOSITORY" -> 3;
            case "MODEL" -> 4;
            case "INTERFACE" -> 5;
            default -> 6;
        };
    }

    private record TypeContext(ParsedType type, KnowledgeNode node) {
    }
}
