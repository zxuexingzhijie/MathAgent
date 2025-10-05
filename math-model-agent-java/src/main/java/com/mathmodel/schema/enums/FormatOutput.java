package com.mathmodel.schema.enums;

/**
 * Output Format Enum
 */
public enum FormatOutput {
    MARKDOWN("markdown", "Markdown 格式"),
    LATEX("latex", "LaTeX 格式"),
    WORD("word", "Word 格式");

    private final String value;
    private final String description;

    FormatOutput(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static FormatOutput fromValue(String value) {
        for (FormatOutput format : FormatOutput.values()) {
            if (format.value.equals(value)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown format: " + value);
    }
}
