package io.github.nilskasseckert.statemachine.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StateTransitionContext {
    private String currentState;
    private Boolean completedSuccessfully;
    private Map<String, Object> variables;
}
