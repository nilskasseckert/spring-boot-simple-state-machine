package io.github.nilskasseckert.statemachine.control;

import io.github.nilskasseckert.statemachine.annotation.StateMachineComponent;
import io.github.nilskasseckert.statemachine.entity.*;
import io.github.nilskasseckert.statemachine.exception.StateMachineInvalidStateException;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Objects;

@StateMachineComponent
@AllArgsConstructor
public class EvaluateStateTransitionForNextStateComponent {

    private final EvaluateConditionalTransitionComponent conditionalTransitionEvaluation;

    public String execute(StateTransitionContext context, List<AbstractTransitionEntity> transitions) {

        val nextState = transitions.stream()
                .map(entry -> handleTransition(context, entry))
                .filter(Objects::nonNull)
                .findFirst();

        if (nextState.isEmpty()) {
            throw new StateMachineInvalidStateException(String.format(
                    "StateMachine: No valid next state found for current state '%s'. Defined transitions: %s",
                    context.getCurrentState(),
                    transitions));
        }

        return nextState.get();
    }

    // helper
    private String handleTransition(StateTransitionContext context, AbstractTransitionEntity transitionEntry) {
        if (transitionEntry instanceof SuccessTransitionEntity) {
            return handleSuccessTransitionEntry(context.getCompletedSuccessfully(), (SuccessTransitionEntity) transitionEntry);
        }

        if (transitionEntry instanceof ErrorTransitionEntity) {
            return handleErrorTransitionEntry(context.getCompletedSuccessfully(), (ErrorTransitionEntity) transitionEntry);
        }

        if (transitionEntry instanceof ConditionalTransitionEntity) {
            return handleConditionalTransitionEntry(context, (ConditionalTransitionEntity) transitionEntry);
        }

        return null;
    }

    private String handleSuccessTransitionEntry(Boolean handlerCompletedSuccessfully, SuccessTransitionEntity transitionEntry) {
        if (handlerCompletedSuccessfully) {
            return transitionEntry.getTo();
        }

        return null;
    }

    private String handleErrorTransitionEntry(Boolean handlerCompletedSuccessfully, ErrorTransitionEntity transitionEntry) {
        if (!handlerCompletedSuccessfully) {
            return transitionEntry.getTo();
        }

        return null;
    }

    private String handleConditionalTransitionEntry(StateTransitionContext context, ConditionalTransitionEntity transitionEntry) {
        if (!context.getCompletedSuccessfully()) {
            return null;
        }

        return conditionalTransitionEvaluation.execute(context, transitionEntry);
    }

}
