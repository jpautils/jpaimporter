package io.github.jpautils.jpaimporter.dto.entity;

import java.util.List;
import java.util.Map;

public class EntityAttributeValueDto {
    private final boolean isValueSet;

    private final boolean shouldAppendCollection;

    private final String stringValue;

    private final List<String> stringValueList;

    private final Map<String, String> stringValueMap;

    private final Object entityValue;

    private final List<?> entityValueList;

    private final Map<?, ?> entityValueMap;

    public EntityAttributeValueDto(
            boolean isValueSet,
            boolean shouldAppendCollection,
            String stringValue,
            List<String> stringValueList,
            Map<String, String> stringValueMap,
            Object entityValue,
            List<?> entityValueList,
            Map<?, ?> entityValueMap
    ) {
        this.isValueSet = isValueSet;
        this.shouldAppendCollection = shouldAppendCollection;
        this.stringValue = stringValue;
        this.stringValueList = stringValueList;
        this.stringValueMap = stringValueMap;
        this.entityValue = entityValue;
        this.entityValueList = entityValueList;
        this.entityValueMap = entityValueMap;
    }

    public boolean getIsValueSet() {
        return isValueSet;
    }

    public boolean getShouldAppendCollection() {
        return shouldAppendCollection;
    }

    public String getStringValue() {
        return stringValue;
    }

    public List<String> getStringValueList() {
        return stringValueList;
    }

    public Map<String, String> getStringValueMap() {
        return stringValueMap;
    }

    public Object getEntityValue() {
        return entityValue;
    }

    public List<?> getEntityValueList() {
        return entityValueList;
    }

    public Map<?, ?> getEntityValueMap() {
        return entityValueMap;
    }
}
