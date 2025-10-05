package com.mathmodel.schema.a2a;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Coordinator to Modeler Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatorToModeler {

    /**
     * Questions map (question number -> question content)
     */
    private Map<String, Object> questions;

    /**
     * Number of questions
     */
    @JsonProperty("ques_count")
    private Integer quesCount;
}
