package io.github.jpautils.jpaimporter.dto.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class EntityAttributeCharacteristicsDto {
    private final Field attributeField;
    private final Method setterMethod;
    private final Method getterMethod;
    private final Type setterType;
    private final Type getterType;
    private final Class<?> entityClass;

    public EntityAttributeCharacteristicsDto(
            Field attributeField,
            Method setterMethod,
            Method getterMethod,
            Type setterType,
            Type getterType,
            Class<?> entityClass
    ) {
        this.attributeField = attributeField;
        this.setterMethod = setterMethod;
        this.setterType = setterType;
        this.getterMethod = getterMethod;
        this.getterType = getterType;
        this.entityClass = entityClass;
    }

    public Field getAttributeField() {
        return attributeField;
    }

    public Method getSetterMethod() {
        return setterMethod;
    }

    public Type getSetterType() {
        return setterType;
    }

    public Method getGetterMethod() {
        return getterMethod;
    }

    public Type getGetterType() {
        return getterType;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }
}
