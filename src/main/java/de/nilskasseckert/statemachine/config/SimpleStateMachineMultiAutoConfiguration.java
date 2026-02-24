package de.nilskasseckert.statemachine.config;

import de.nilskasseckert.statemachine.SimpleStateMachineFactory;
import de.nilskasseckert.statemachine.SimpleStateMachineService;
import de.nilskasseckert.statemachine.control.EvaluateConditionalTransitionComponent;
import de.nilskasseckert.statemachine.control.EvaluateStateTransitionForNextStateComponent;
import de.nilskasseckert.statemachine.entity.StateMachineConfig;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
@Conditional(SimpleStateMachineMultiAutoConfiguration.OnDefinitionsCondition.class)
public class SimpleStateMachineMultiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    SimpleStateMachineFactory simpleStateMachineFactory(
            ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher,
            org.springframework.core.env.Environment environment) throws Exception {

        val definitions = Binder.get(environment)
                .bind("simple-state-machine.definitions", Bindable.mapOf(String.class, String.class))
                .orElse(Map.of());

        val conditionalEval = new EvaluateConditionalTransitionComponent();
        val transitionEval = new EvaluateStateTransitionForNextStateComponent(conditionalEval);

        Map<String, SimpleStateMachineService> stateMachines = new HashMap<>();

        for (var entry : definitions.entrySet()) {
            val resource = new ClassPathResource(entry.getValue());
            StateMachineConfig config;

            try (InputStream stream = resource.getInputStream()) {
                config = objectMapper.readValue(stream, StateMachineConfig.class);
            }

            stateMachines.put(entry.getKey(),
                    new SimpleStateMachineService(config, eventPublisher, transitionEval));
        }

        return new SimpleStateMachineFactory(stateMachines);
    }

    static class OnDefinitionsCondition implements org.springframework.context.annotation.Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return !Binder.get(context.getEnvironment())
                    .bind("simple-state-machine.definitions", Bindable.mapOf(String.class, String.class))
                    .orElse(Map.of())
                    .isEmpty();
        }
    }
}
