package io.github.jpautils.jpaimporter.dto.parsing;

import java.util.List;

public class ParsedAttributeValuesRowDto {
    private final List<ParsedAttributeValueDto> attributeValues;

    private final String entityReferenceName;

    public ParsedAttributeValuesRowDto(List<ParsedAttributeValueDto> entityValues, String entityReferenceName) {
        this.attributeValues = entityValues;
        this.entityReferenceName = entityReferenceName;
    }

    public List<ParsedAttributeValueDto> getAttributeValues() {
        return attributeValues;
    }

    public String getEntityReferenceName() {
        return entityReferenceName;
    }
}
