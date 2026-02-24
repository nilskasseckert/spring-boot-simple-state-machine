package de.nilskasseckert.statemachine;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class SimpleStateMachineFactory {

    private final Map<String, SimpleStateMachineService> stateMachines;

    public SimpleStateMachineFactory(Map<String, SimpleStateMachineService> stateMachines) {
        this.stateMachines = stateMachines;
    }

    public SimpleStateMachineService get(String name) {
        SimpleStateMachineService service = stateMachines.get(name);

        if (service == null) {
            throw new IllegalArgumentException(String.format(
                    "StateMachine: No state machine found with name '%s'. Available: %s",
                    name, stateMachines.keySet()));
        }

        return service;
    }

    public Set<String> getStateMachineNames() {
        return Collections.unmodifiableSet(stateMachines.keySet());
    }
}
