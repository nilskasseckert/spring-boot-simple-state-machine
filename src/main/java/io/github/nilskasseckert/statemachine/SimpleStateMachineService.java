package io.github.nilskasseckert.statemachine;

import io.github.nilskasseckert.statemachine.annotation.StateMachineService;
import io.github.nilskasseckert.statemachine.control.EvaluateStateTransitionForNextStateComponent;
import io.github.nilskasseckert.statemachine.entity.*;
import io.github.nilskasseckert.statemachine.event.StateMachineIllegalActionEvent;
import io.github.nilskasseckert.statemachine.event.StateMachineInvalidStateEvent;
import io.github.nilskasseckert.statemachine.exception.StateMachineIllegalActionException;
import io.github.nilskasseckert.statemachine.exception.StateMachineInvalidStateException;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@StateMachineService
public class SimpleStateMachineService {

    private final Map<String, List<AbstractTransitionEntity>> stateTransitions;
    private final Map<String, StateEntity> stateEntityMap;

    private final ApplicationEventPublisher eventPublisher;
    private final EvaluateStateTransitionForNextStateComponent evaluateStateTransitionForNextState;

    public SimpleStateMachineService(
            StateMachineConfig stateMachineConfig,
            ApplicationEventPublisher eventPublisher,
            EvaluateStateTransitionForNextStateComponent evaluateStateTransitionForNextState
    ) {
        this.eventPublisher = eventPublisher;
        this.evaluateStateTransitionForNextState = evaluateStateTransitionForNextState;

        stateTransitions = stateMachineConfig.getTransitions().stream().collect(
                Collectors.groupingBy(AbstractTransitionEntity::getFrom));

        stateEntityMap = stateMachineConfig.getStates().stream().collect(
                Collectors.toMap(StateEntity::getState, e -> e));
    }

    public void requireActionAllowed(String currentState, String action) {
        if (!isActionAllowedForState(currentState, action)) {
            eventPublisher.publishEvent(new StateMachineIllegalActionEvent(currentState, action));

            throw new StateMachineIllegalActionException(String.format(
                    "StateMachine: Action '%s' is not allowed in current state '%s'.",
                    action, currentState));
        }
    }

    public boolean isActionAllowedForState(String state, String action) {
        val stateEntry = stateEntityMap.get(state);

        if (stateEntry == null) {
            return false;
        }

        return stateEntry.getAllowedActions().contains(action);
    }

    public void requireState(String currentState, String expectedState) {
        if (!currentState.equals(expectedState)) {
            eventPublisher.publishEvent(new StateMachineInvalidStateEvent(currentState, List.of(expectedState)));

            throw new StateMachineInvalidStateException(String.format(
                    "StateMachine: Expected state '%s' does not match actual state '%s'.",
                    expectedState, currentState));
        }
    }

    public void requireOneStateOf(String currentState, String... expectedStates) {
        if (!isInOneStateOf(currentState, expectedStates)) {
            val expectedStateList = Arrays.asList(expectedStates);

            eventPublisher.publishEvent(new StateMachineInvalidStateEvent(currentState, expectedStateList));

            throw new StateMachineInvalidStateException(String.format(
                    "StateMachine: Expected states %s do not contain actual state '%s'.",
                    expectedStateList, currentState));
        }
    }

    public boolean isInOneStateOf(String currentState, String... expectedStates) {
        if (expectedStates.length == 0) {
            throw new StateMachineInvalidStateException(String.format(
                    "StateMachine: Invalid call - no expected states provided (current state: '%s').",
                    currentState));
        }

        val allStates = Arrays.asList(expectedStates);
        return allStates.contains(currentState);
    }

    public String nextStateForSuccess(String currentState) {
        return nextState(currentState, true, Map.of());
    }

    public String nextStateForSuccess(String currentState, Map<String, Object> variables) {
        return nextState(currentState, true, variables);
    }

    public String nextStateForError(String currentState) {
        return nextState(currentState, false, Map.of());
    }

    public String nextStateForError(String currentState, Map<String, Object> variables) {
        return nextState(currentState, false, variables);
    }

    // helper
    private String nextState(String currentState, boolean completedSuccessfully, Map<String, Object> variables) {
        val transitions = stateTransitions.get(currentState);

        if (transitions == null) {
            throw new StateMachineInvalidStateException(String.format(
                    "StateMachine: No transitions defined for state '%s'.", currentState));
        }

        StateTransitionContext context = new StateTransitionContext(
                currentState,
                completedSuccessfully,
                variables
        );

        return evaluateStateTransitionForNextState.execute(context, transitions);
    }
}
