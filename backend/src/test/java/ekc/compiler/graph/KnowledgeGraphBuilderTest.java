package ekc.compiler.graph;

import ekc.compiler.ast.AstExtractorImpl;
import ekc.compiler.ast.AstParserImpl;
import ekc.shared.model.acquisition.CompilerContext;
import ekc.shared.model.acquisition.RepositoryMetadata;
import ekc.shared.model.graph.KnowledgeEdge;
import ekc.shared.model.graph.KnowledgeNode;
import ekc.shared.model.graph.RepositoryKnowledgeGraph;
import ekc.shared.model.source.RepositorySource;
import ekc.shared.model.source.SourceFile;
import ekc.shared.model.source.SourceLanguage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeGraphBuilderTest {
    @TempDir
    Path repositoryRoot;

    @Test
    void mapsSpringLayersRelationshipsAndClassInternalsFromParsedEvidence() throws Exception {
        Path sourcePath = repositoryRoot.resolve("src/main/java/demo/SystemTypes.java");
        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, """
                package demo;
                import java.util.List;
                @SpringBootApplication class DemoApplication {}
                @RestController class OrderController {
                    private OrderService service;
                    OrderDto get(OrderRequest request) {
                        OrderDto response = service.load();
                        return response;
                    }
                }
                @Service class OrderService { private OrderRepository repository; OrderDto load() { return null; } }
                @Repository class OrderRepository implements OrderStore {}
                interface OrderStore {}
                class OrderDto {}
                class OrderRequest {}
                """);
        RepositorySource source = new RepositorySource(
                List.of(new SourceFile(sourcePath, "src/main/java/demo/SystemTypes.java", SourceLanguage.JAVA)),
                Map.of(SourceLanguage.JAVA, 1));
        CompilerContext context = new CompilerContext(
                new RepositoryMetadata("demo", "main", repositoryRoot), source, null, null);
        context = new AstParserImpl().parse(context);
        context = new AstExtractorImpl().extract(context);

        RepositoryKnowledgeGraph graph = new KnowledgeGraphBuilder().build(context);

        assertThat(graph.nodes()).extracting(KnowledgeNode::kind)
                .contains("APPLICATION", "CONTROLLER", "SERVICE", "REPOSITORY", "INTERFACE");
        assertThat(graph.edges()).anySatisfy(edge -> assertEdge(
                edge, "OrderController", "OrderService", "DEPENDS_ON"));
        assertThat(graph.edges()).anySatisfy(edge -> assertEdge(
                edge, "OrderService", "OrderRepository", "DEPENDS_ON"));
        assertThat(graph.edges()).anySatisfy(edge -> assertEdge(
                edge, "OrderRepository", "OrderStore", "IMPLEMENTS"));
        assertThat(graph.edges()).anySatisfy(edge -> assertEdge(
                edge, "DemoApplication", "OrderController", "DISCOVERS"));
        KnowledgeNode controller = graph.nodes().stream()
                .filter(node -> node.label().equals("OrderController"))
                .findFirst().orElseThrow();
        assertThat(controller.imports()).contains("java.util.List");
        assertThat(controller.fields()).singleElement().satisfies(field -> {
            assertThat(field.name()).isEqualTo("service");
            assertThat(field.type()).isEqualTo("OrderService");
        });
        assertThat(controller.methods()).singleElement().satisfies(method -> {
            assertThat(method.parameters()).singleElement().satisfies(parameter ->
                    assertThat(parameter.name()).isEqualTo("request"));
            assertThat(method.localVariables()).singleElement().satisfies(variable ->
                    assertThat(variable.name()).isEqualTo("response"));
        });
    }

    private void assertEdge(KnowledgeEdge edge, String source, String target, String kind) {
        assertThat(edge.source()).endsWith("." + source);
        assertThat(edge.target()).endsWith("." + target);
        assertThat(edge.kind()).isEqualTo(kind);
    }
}
