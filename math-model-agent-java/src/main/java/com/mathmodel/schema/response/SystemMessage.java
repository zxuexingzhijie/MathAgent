package com.mathmodel.schema.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * System Message Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMessage {

    /**
     * Message content
     */
    private String content;

    /**
     * Message type: info, success, warning, error
     */
    @Builder.Default
    private String type = "info";

    /**
     * Timestamp
     */
    @JsonProperty("timestamp")
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    public static SystemMessage info(String content) {
        return SystemMessage.builder()
                .content(content)
                .type("info")
                .build();
    }

    public static SystemMessage success(String content) {
        return SystemMessage.builder()
                .content(content)
                .type("success")
                .build();
    }

    public static SystemMessage warning(String content) {
        return SystemMessage.builder()
                .content(content)
                .type("warning")
                .build();
    }

    public static SystemMessage error(String content) {
        return SystemMessage.builder()
                .content(content)
                .type("error")
                .build();
    }
}
