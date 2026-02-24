package io.github.nilskasseckert.statemachine.control;

import io.github.nilskasseckert.statemachine.annotation.StateMachineComponent;
import io.github.nilskasseckert.statemachine.entity.ConditionalTransitionEntity;
import io.github.nilskasseckert.statemachine.entity.StateTransitionContext;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Comparator;

@StateMachineComponent
@NoArgsConstructor
public class EvaluateConditionalTransitionComponent {

    private final ExpressionParser parser = new SpelExpressionParser();

    public String execute(StateTransitionContext context, ConditionalTransitionEntity conditionalTransition) {

        if (!context.getCompletedSuccessfully()) {
            return null;
        }

        val evaluationContext = createEvaluationContext(context);

        val nextStateByCondition = conditionalTransition.getConditions().stream()
                .sorted(Comparator.comparingInt(ConditionalTransitionEntity.Condition::getOrder))
                .filter((condition) -> handleEvaluateCondition(evaluationContext, condition))
                .map(ConditionalTransitionEntity.Condition::getTo)
                .findFirst();

        return nextStateByCondition.orElse(null);
    }

    // helper
    private Boolean handleEvaluateCondition(EvaluationContext evaluationContext, ConditionalTransitionEntity.Condition condition) {
        if (condition instanceof ConditionalTransitionEntity.ConditionWhen) {
            return handleWhenCondition(evaluationContext, (ConditionalTransitionEntity.ConditionWhen) condition);
        }

        if (condition instanceof ConditionalTransitionEntity.ConditionElse) {
            return true;
        }

        return null;
    }

    private Boolean handleWhenCondition(EvaluationContext evaluationContext, ConditionalTransitionEntity.ConditionWhen condition) {
        val expression = parser.parseExpression(condition.getWhen());
        return expression.getValue(evaluationContext, Boolean.class);
    }

    private StandardEvaluationContext createEvaluationContext(StateTransitionContext context) {
        val evaluationContext = new StandardEvaluationContext();

        if (context.getVariables() != null) {
            context.getVariables().forEach(evaluationContext::setVariable);
        }

        return evaluationContext;
    }

}
