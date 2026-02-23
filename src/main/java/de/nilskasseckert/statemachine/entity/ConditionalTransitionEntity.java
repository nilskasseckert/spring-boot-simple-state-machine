package de.nilskasseckert.statemachine.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConditionalTransitionEntity extends AbstractTransitionEntity {
    private List<Condition> conditions;

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({
            @JsonSubTypes.Type(ConditionWhen.class),
            @JsonSubTypes.Type(ConditionElse.class)
    })
    public interface Condition {
        int getOrder();
        String getTo();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConditionWhen implements Condition {
        private String when;
        private String to;

        public int getOrder() {
            return 1;
        }

        @Override
        public String getTo() {
            return to;
        }

        public String getWhen() {
            return when;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConditionElse implements Condition {
        @JsonProperty("else")
        private String to;

        public int getOrder() {
            return 2;
        }

        @Override
        public String getTo() {
            return to;
        }
    }
}
