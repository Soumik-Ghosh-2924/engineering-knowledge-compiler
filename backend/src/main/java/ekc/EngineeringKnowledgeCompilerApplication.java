package ekc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EngineeringKnowledgeCompilerApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                EngineeringKnowledgeCompilerApplication.class,
                args);
    }

}