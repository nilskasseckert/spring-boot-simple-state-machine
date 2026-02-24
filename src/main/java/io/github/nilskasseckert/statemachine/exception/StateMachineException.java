package io.github.nilskasseckert.statemachine.exception;

public abstract class StateMachineException extends RuntimeException {
    protected StateMachineException(String message) {
        super(message);
    }
}
