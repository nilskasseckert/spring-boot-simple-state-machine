package io.github.nilskasseckert.statemachine.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SuccessTransitionEntity.class, name = "SUCCESS"),
        @JsonSubTypes.Type(value = ErrorTransitionEntity.class, name = "ERROR"),
        @JsonSubTypes.Type(value = ConditionalTransitionEntity.class, name = "CONDITIONAL"),
})
public abstract class AbstractTransitionEntity implements Serializable {
    private String from;
    private TransitionType type;
}
