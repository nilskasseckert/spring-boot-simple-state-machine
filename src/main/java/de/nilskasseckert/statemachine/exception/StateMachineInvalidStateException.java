package de.nilskasseckert.statemachine.exception;

public class StateMachineInvalidStateException extends StateMachineException {
    public StateMachineInvalidStateException(String msg) {
        super(msg);
    }
}
