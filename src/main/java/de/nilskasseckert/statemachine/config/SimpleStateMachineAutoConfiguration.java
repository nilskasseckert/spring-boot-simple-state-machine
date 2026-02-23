package de.nilskasseckert.statemachine.config;

import de.nilskasseckert.statemachine.entity.StateMachineConfig;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;

@AutoConfiguration
@ComponentScan("de.nilskasseckert.statemachine")
@ConditionalOnProperty(name = "simple-state-machine.definition", matchIfMissing = false)
public class SimpleStateMachineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    StateMachineConfig stateMachineConfig(ObjectMapper objectMapper,
                                          org.springframework.core.env.Environment environment) throws Exception {
        val path = environment.getRequiredProperty("simple-state-machine.definition");
        val resource = new ClassPathResource(path);

        try (InputStream stream = resource.getInputStream()) {
            return objectMapper.readValue(stream, StateMachineConfig.class);
        }
    }
}
