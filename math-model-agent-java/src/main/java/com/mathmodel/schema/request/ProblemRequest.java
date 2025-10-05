package com.mathmodel.schema.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mathmodel.schema.enums.CompTemplate;
import com.mathmodel.schema.enums.FormatOutput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Problem Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemRequest {

    /**
     * Task ID
     */
    @NotBlank(message = "taskId cannot be blank")
    @JsonProperty("task_id")
    private String taskId;

    /**
     * All questions/problem statement
     */
    @NotBlank(message = "quesAll cannot be blank")
    @JsonProperty("ques_all")
    private String quesAll;

    /**
     * Competition template
     */
    @NotNull(message = "compTemplate cannot be null")
    @JsonProperty("comp_template")
    @Builder.Default
    private CompTemplate compTemplate = CompTemplate.CHINA;

    /**
     * Output format
     */
    @NotNull(message = "formatOutput cannot be null")
    @JsonProperty("format_output")
    @Builder.Default
    private FormatOutput formatOutput = FormatOutput.MARKDOWN;
}
