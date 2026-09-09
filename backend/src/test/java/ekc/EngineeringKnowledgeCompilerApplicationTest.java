package ekc;

import ekc.compiler.CompilerEngine;
import ekc.compiler.CompilerEngineImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EngineeringKnowledgeCompilerApplicationTest {

    @Autowired
    CompilerEngine compilerEngine;

    @Test
    void startsWithTheProductionCompilerEngine() {
        assertThat(compilerEngine).isInstanceOf(CompilerEngineImpl.class);
    }
}
