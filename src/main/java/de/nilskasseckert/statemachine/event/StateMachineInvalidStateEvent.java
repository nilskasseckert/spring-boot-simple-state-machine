package de.nilskasseckert.statemachine.event;

import java.util.List;

public record StateMachineInvalidStateEvent(
        String currentState,
        List<String> expectedStates
) {
}
