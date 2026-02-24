package io.github.nilskasseckert.statemachine.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StateEntity implements Serializable {
    private String state;
    private List<String> allowedActions;
}
