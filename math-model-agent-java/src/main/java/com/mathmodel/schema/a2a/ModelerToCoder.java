package com.mathmodel.schema.a2a;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Modeler to Coder Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelerToCoder {

    /**
     * Questions solution map (question -> solution approach)
     */
    @JsonProperty("questions_solution")
    private Map<String, Object> questionsSolution;
}
