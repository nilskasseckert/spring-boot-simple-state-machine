package io.github.nilskasseckert.statemachine.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorTransitionEntity extends AbstractTransitionEntity {
    private String to;
}
