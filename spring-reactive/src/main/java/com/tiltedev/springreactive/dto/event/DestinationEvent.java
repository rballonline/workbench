package com.tiltedev.springreactive.dto.event;

import com.tiltedev.springreactive.model.Destination;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DestinationEvent {

    public enum Action {
        CREATED,
        UPDATED,
        DELETED
    }

    private Action action;
    private Destination destination;
}
