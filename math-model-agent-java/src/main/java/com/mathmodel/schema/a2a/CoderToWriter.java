package com.mathmodel.schema.a2a;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Coder to Writer Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoderToWriter {

    /**
     * Code response/result
     */
    @JsonProperty("code_response")
    private String codeResponse;

    /**
     * Created images during code execution
     */
    @JsonProperty("created_images")
    private List<String> createdImages;

    /**
     * Execution output
     */
    @JsonProperty("execution_output")
    private String executionOutput;
}
