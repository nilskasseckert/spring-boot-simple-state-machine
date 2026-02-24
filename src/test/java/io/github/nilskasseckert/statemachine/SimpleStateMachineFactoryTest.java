package io.github.nilskasseckert.statemachine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "simple-state-machine.definitions.order=state-machine/order.json",
        "simple-state-machine.definitions.payment=state-machine/payment.json"
})
class SimpleStateMachineFactoryTest {

    @Autowired
    SimpleStateMachineFactory stateMachineFactory;

    @Test
    void shouldHaveTwoStateMachines() {
        assertEquals(2, stateMachineFactory.getStateMachineNames().size());
        assertTrue(stateMachineFactory.getStateMachineNames().contains("order"));
        assertTrue(stateMachineFactory.getStateMachineNames().contains("payment"));
    }

    @Test
    void shouldTransitionOrderStateMachine() {
        var orderStateMachine = stateMachineFactory.get("order");
        var nextState = orderStateMachine.nextStateForSuccess("CREATED");
        assertEquals("PROCESSING", nextState);
    }

    @Test
    void shouldTransitionPaymentStateMachine() {
        var paymentStateMachine = stateMachineFactory.get("payment");
        var nextState = paymentStateMachine.nextStateForSuccess("PENDING");
        assertEquals("AUTHORIZED", nextState);
    }

    @Test
    void shouldTransitionPaymentOnError() {
        var paymentStateMachine = stateMachineFactory.get("payment");
        var nextState = paymentStateMachine.nextStateForError("PENDING");
        assertEquals("FAILED", nextState);
    }

    @Test
    void shouldKeepStateMachinesIndependent() {
        var orderStateMachine = stateMachineFactory.get("order");
        var paymentStateMachine = stateMachineFactory.get("payment");

        // Order states should not exist in payment and vice versa
        assertFalse(paymentStateMachine.isActionAllowedForState("REVIEW", "APPROVE"));
        assertTrue(orderStateMachine.isActionAllowedForState("REVIEW", "APPROVE"));

        assertFalse(orderStateMachine.isActionAllowedForState("AUTHORIZED", "CAPTURE"));
        assertTrue(paymentStateMachine.isActionAllowedForState("AUTHORIZED", "CAPTURE"));
    }

    @Test
    void shouldThrowOnUnknownStateMachine() {
        assertThrows(IllegalArgumentException.class, () ->
                stateMachineFactory.get("unknown"));
    }
}
