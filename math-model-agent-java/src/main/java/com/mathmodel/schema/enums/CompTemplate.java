package com.mathmodel.schema.enums;

/**
 * Competition Template Enum
 */
public enum CompTemplate {
    CHINA("china", "中国数学建模竞赛"),
    USA("usa", "美国大学生数学建模竞赛"),
    CUSTOM("custom", "自定义模板");

    private final String value;
    private final String description;

    CompTemplate(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static CompTemplate fromValue(String value) {
        for (CompTemplate template : CompTemplate.values()) {
            if (template.value.equals(value)) {
                return template;
            }
        }
        throw new IllegalArgumentException("Unknown template: " + value);
    }
}
