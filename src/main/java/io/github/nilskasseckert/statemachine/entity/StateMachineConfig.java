package io.github.nilskasseckert.statemachine.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class StateMachineConfig {
    private List<StateEntity> states;
    private List<AbstractTransitionEntity> transitions;
}
