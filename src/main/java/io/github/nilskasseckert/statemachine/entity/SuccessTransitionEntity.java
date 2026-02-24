package io.github.nilskasseckert.statemachine.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuccessTransitionEntity extends AbstractTransitionEntity {
    private String to;
}
