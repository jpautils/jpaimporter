package io.github.jpautils.jpaimporter.attribute.setter.impl;

import io.github.jpautils.jpaimporter.attribute.setter.EntityAttributeSetter;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeCharacteristicsDto;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeValueDto;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class ReferencedEntitySetEntityAttributeSetter implements EntityAttributeSetter {
    @Override
    public void setEntityAttributeValue(EntityAttributeCharacteristicsDto entityAttributeCharacteristics, EntityAttributeValueDto entityAttributeValue, Object entity) {
        if (!entityAttributeValue.getIsValueSet()) {
            return;
        }

        Method setterMethod = entityAttributeCharacteristics.getSetterMethod();
        Method getterMethod = entityAttributeCharacteristics.getGetterMethod();
        Field attributeField = entityAttributeCharacteristics.getAttributeField();
        Type setterType = entityAttributeCharacteristics.getSetterType();
        Type getterType = entityAttributeCharacteristics.getGetterType();

        Set<?> valueToBeSet;

        try {

            if (entityAttributeValue.getShouldAppendCollection()) {
                if (!setterType.getTypeName().equalsIgnoreCase(getterType.getTypeName())){
                    throw new RuntimeException("When trying to append collection, the setter and getter types should match. " +
                            "Class: [" + entity.getClass() + "], field: [" + entityAttributeCharacteristics.getAttributeField() + "], " +
                            "setter type: [" + setterType + "], getter type: [" + getterType + "]");
                }

                Set<Object> currentValue;

                if (getterMethod != null) {
                    currentValue = (Set<Object>) getterMethod.invoke(entity);
                } else {
                    currentValue = (Set<Object>) attributeField.get(entity);
                }

                if (currentValue != null) {
                    Set<Object> newValue = new HashSet<>();
                    newValue.addAll(currentValue);
                    newValue.addAll(entityAttributeValue.getEntityValueList());
                    valueToBeSet = newValue;
                } else {
                    valueToBeSet = new HashSet<>(entityAttributeValue.getEntityValueList());
                }
            } else {
                valueToBeSet = new HashSet<>(entityAttributeValue.getEntityValueList());
            }

            if (setterMethod != null) {
                setterMethod.invoke(entity, valueToBeSet);
            } else if (attributeField != null) {
                attributeField.set(entity, valueToBeSet);
            } else {
                throw new RuntimeException("Could not find a setter method/field for attribute: " + entityAttributeCharacteristics.getAttributeField().getName() + "and class: " + entityAttributeCharacteristics.getEntityClass());
            }

        } catch (Exception exception) {
            throw new RuntimeException("Could not set value for attribute [" + entityAttributeCharacteristics.getAttributeField().getName() + "] of class [" + entityAttributeCharacteristics.getEntityClass() + "]. Value: " + entityAttributeValue, exception);
        }
    }

    @Override
    public Predicate insertAttributeInCriteriaQuerySearch(EntityAttributeCharacteristicsDto entityAttributeCharacteristics, EntityAttributeValueDto entityAttributeValue, String fieldName, CriteriaBuilder criteriaBuilder, Root<?> root) {
        throw new RuntimeException("Searching for an entity by a set of entities as a parameter is not supported.");
    }

    @Override
    public boolean matchesEntityAttribute(EntityAttributeCharacteristicsDto entityAttributeCharacteristics) {
        if (entityAttributeCharacteristics.getSetterType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) entityAttributeCharacteristics.getSetterType();

            if (parameterizedType.getActualTypeArguments().length == 1 && parameterizedType.getRawType() instanceof Class<?>) {
                Class<?> parametrizedRawType = (Class<?>) parameterizedType.getRawType();

                if (
                        Set.class.isAssignableFrom(parametrizedRawType)
                                && parameterizedType.getActualTypeArguments()[0] instanceof Class<?>
                ) {
                    Class<?> typeArgument = (Class<?>) parameterizedType.getActualTypeArguments()[0];

                    if (typeArgument.getAnnotation(Entity.class) != null || typeArgument.getAnnotation(MappedSuperclass.class) != null) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public BigDecimal getPriority() {
        return new BigDecimal(10);
    }
}
