package io.github.jpautils.jpaimporter.dto.entity;


import io.github.jpautils.jpaimporter.attribute.setter.EntityAttributeSetter;

import java.util.List;

public class EntityAttributeDto {
    private final String attributeName;
    private final boolean isUnique;
    private final String attributeIsAMapValueForMapKeyName;
    private final EntityAttributeSetter entityAttributeSetter;
    private final EntityAttributeCharacteristicsDto entityAttributeCharacteristics;
    private final boolean isAttributeReferencingEntity;
    private final Class<?> referencedEntityClass;
    private final List<String> referencedEntityUniqueAttributes;
    private final List<EntityAttributeSetter> referencedEntityUniqueAttributesSetters;
    private final List<EntityAttributeCharacteristicsDto> referencedEntityUniqueAttributesCharacteristics;


    public EntityAttributeDto(
            String attributeName,
            boolean isUnique,
            String attributeIsAMapValueForMapKeyName,
            EntityAttributeSetter entityAttributeSetter,
            EntityAttributeCharacteristicsDto entityAttributeCharacteristics,
            boolean isAttributeReferencingEntity,
            Class<?> referencedEntityClass,
            List<String> referencedEntityUniqueAttributes,
            List<EntityAttributeSetter> referencedEntityUniqueAttributesSetters,
            List<EntityAttributeCharacteristicsDto> referencedEntityUniqueAttributesCharacteristics
    ) {
        this.attributeName = attributeName;
        this.isUnique = isUnique;
        this.attributeIsAMapValueForMapKeyName = attributeIsAMapValueForMapKeyName;
        this.entityAttributeSetter = entityAttributeSetter;
        this.entityAttributeCharacteristics = entityAttributeCharacteristics;
        this.isAttributeReferencingEntity = isAttributeReferencingEntity;
        this.referencedEntityUniqueAttributes = referencedEntityUniqueAttributes;
        this.referencedEntityClass = referencedEntityClass;
        this.referencedEntityUniqueAttributesSetters = referencedEntityUniqueAttributesSetters;
        this.referencedEntityUniqueAttributesCharacteristics = referencedEntityUniqueAttributesCharacteristics;
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

    public EntityAttributeSetter getEntityAttributeSetter() {
        return entityAttributeSetter;
    }

    public EntityAttributeCharacteristicsDto getEntityAttributeCharacteristics() {
        return entityAttributeCharacteristics;
    }

    public boolean getIsAttributeReferencingEntity() {
        return isAttributeReferencingEntity;
    }

    public Class<?> getReferencedEntityClass() {
        return referencedEntityClass;
    }

    public List<String> getReferencedEntityUniqueAttributes() {
        return referencedEntityUniqueAttributes;
    }

    public List<EntityAttributeSetter> getReferencedEntityUniqueAttributesSetters() {
        return referencedEntityUniqueAttributesSetters;
    }

    public List<EntityAttributeCharacteristicsDto> getReferencedEntityUniqueAttributesCharacteristics() {
        return referencedEntityUniqueAttributesCharacteristics;
    }
}
