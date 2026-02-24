package io.github.nilskasseckert.statemachine.event;

public record StateMachineIllegalActionEvent(
        String currentState,
        String action
) {
}
