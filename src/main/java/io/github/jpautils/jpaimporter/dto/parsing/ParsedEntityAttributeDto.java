package io.github.jpautils.jpaimporter.dto.parsing;

import java.util.List;

public class ParsedEntityAttributeDto {
    private final String attributeName;
    private final boolean isUnique;
    private final String attributeIsAMapValueForMapKeyName;
    private final boolean isAttributeReferencingEntity;
    private final List<String> referencedEntityUniqueAttributes;

    public ParsedEntityAttributeDto(
            String attributeName,
            boolean isUnique,
            String attributeIsAMapValueForMapKeyName,
            boolean isAttributeReferencingEntity,
            List<String> referencedEntityUniqueAttributes
    ) {
        this.attributeName = attributeName;
        this.isUnique = isUnique;
        this.attributeIsAMapValueForMapKeyName = attributeIsAMapValueForMapKeyName;
        this.isAttributeReferencingEntity = isAttributeReferencingEntity;
        this.referencedEntityUniqueAttributes = referencedEntityUniqueAttributes;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public boolean getIsUnique() {
        return isUnique;
    }

    public String getAttributeIsAMapValueForMapKeyName() {
        return attributeIsAMapValueForMapKeyName;
    }

    public boolean getIsAttributeReferencingEntity() {
        return isAttributeReferencingEntity;
    }

    public List<String> getReferencedEntityUniqueAttributes() {
        return referencedEntityUniqueAttributes;
    }
}
