package com.example.jpacrud.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.pcollections.PSet;

@Value
@Builder
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class UpdateEntityRequest {
    private final String property;
    private final PSet<String> parts;
}
