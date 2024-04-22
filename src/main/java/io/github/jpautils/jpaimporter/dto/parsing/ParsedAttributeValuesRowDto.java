package io.github.jpautils.jpaimporter.dto.parsing;

import java.util.List;

public class ParsedAttributeValuesRowDto {
    private final List<ParsedAttributeValueDto> attributeValues;

    private final String entityVariableNameForStoringInContext;

    public ParsedAttributeValuesRowDto(List<ParsedAttributeValueDto> entityValues, String entityVariableNameForStoringInContext) {
        this.attributeValues = entityValues;
        this.entityVariableNameForStoringInContext = entityVariableNameForStoringInContext;
    }

    public List<ParsedAttributeValueDto> getAttributeValues() {
        return attributeValues;
    }

    public String getEntityVariableNameForStoringInContext() {
        return entityVariableNameForStoringInContext;
    }
}
