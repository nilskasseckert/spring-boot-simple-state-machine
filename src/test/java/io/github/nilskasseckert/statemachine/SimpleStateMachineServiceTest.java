package io.github.nilskasseckert.statemachine;

import io.github.nilskasseckert.statemachine.exception.StateMachineIllegalActionException;
import io.github.nilskasseckert.statemachine.exception.StateMachineInvalidStateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SimpleStateMachineServiceTest {

    @Autowired
    SimpleStateMachineService stateMachineService;

    @Test
    void shouldTransitionOnSuccess() {
        var nextState = stateMachineService.nextStateForSuccess("CREATED");
        assertEquals("PROCESSING", nextState);
    }

    @Test
    void shouldTransitionOnError() {
        var nextState = stateMachineService.nextStateForError("PROCESSING");
        assertEquals("ERROR_PROCESSING", nextState);
    }

    @Test
    void shouldEvaluateConditionalTransition() {
        var nextState = stateMachineService.nextStateForSuccess("PROCESSING",
                Map.of("order", new TestOrder(1500)));
        assertEquals("REVIEW", nextState);
    }

    @Test
    void shouldEvaluateConditionalTransitionElseBranch() {
        var nextState = stateMachineService.nextStateForSuccess("PROCESSING",
                Map.of("order", new TestOrder(500)));
        assertEquals("APPROVED", nextState);
    }

    @Test
    void shouldAllowAction() {
        assertTrue(stateMachineService.isActionAllowedForState("REVIEW", "APPROVE"));
        assertTrue(stateMachineService.isActionAllowedForState("REVIEW", "REJECT"));
    }

    @Test
    void shouldDenyAction() {
        assertFalse(stateMachineService.isActionAllowedForState("CREATED", "APPROVE"));
    }

    @Test
    void shouldThrowOnIllegalAction() {
        assertThrows(StateMachineIllegalActionException.class, () ->
                stateMachineService.requireActionAllowed("CREATED", "APPROVE"));
    }

    @Test
    void shouldRequireState() {
        assertDoesNotThrow(() -> stateMachineService.requireState("CREATED", "CREATED"));
    }

    @Test
    void shouldThrowOnInvalidState() {
        assertThrows(StateMachineInvalidStateException.class, () ->
                stateMachineService.requireState("CREATED", "PROCESSING"));
    }

    @Test
    void shouldRequireOneStateOf() {
        assertDoesNotThrow(() -> stateMachineService.requireOneStateOf("CREATED", "CREATED", "PROCESSING"));
    }

    @Test
    void shouldThrowOnNoMatchingStateOf() {
        assertThrows(StateMachineInvalidStateException.class, () ->
                stateMachineService.requireOneStateOf("CREATED", "PROCESSING", "REVIEW"));
    }

    @Test
    void shouldRecoverFromError() {
        var nextState = stateMachineService.nextStateForSuccess("ERROR_PROCESSING");
        assertEquals("CREATED", nextState);
    }

    public record TestOrder(int totalAmount) {
        public int getTotalAmount() {
            return totalAmount;
        }
    }
}
