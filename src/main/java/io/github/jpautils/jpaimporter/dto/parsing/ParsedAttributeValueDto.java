package io.github.jpautils.jpaimporter.dto.parsing;

import java.util.List;
import java.util.Map;

public class ParsedAttributeValueDto {
    private final String attributeValue;
    private final List<String> attributeValuesList;
    private final Map<String, String> attributeValuesMap;

    public ParsedAttributeValueDto(String attributeValue, List<String> attributeValuesList, Map<String, String> attributeValuesMap) {
        this.attributeValue = attributeValue;
        this.attributeValuesList = attributeValuesList;
        this.attributeValuesMap = attributeValuesMap;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public List<String> getAttributeValuesList() {
        return attributeValuesList;
    }

    public Map<String, String> getAttributeValuesMap() {
        return attributeValuesMap;
    }
}
