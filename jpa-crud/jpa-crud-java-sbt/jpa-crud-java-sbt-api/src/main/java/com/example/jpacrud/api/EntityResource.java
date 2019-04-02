package com.example.jpacrud.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;
import org.pcollections.PSet;

@Value
@Wither
@AllArgsConstructor(staticName = "of", onConstructor = @__({@JsonCreator}))
public class EntityResource {
    private final String id;
    private final String property;
    private final PSet<String> parts;
}
