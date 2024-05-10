package io.github.jpautils.jpaimporter.attribute.setter.impl;

import io.github.jpautils.jpaimporter.attribute.setter.EntityAttributeSetter;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeCharacteristicsDto;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeValueDto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class StringStringMapEntityAttributeSetter implements EntityAttributeSetter {
    @Override
    public void setEntityAttributeValue(EntityAttributeCharacteristicsDto entityAttributeCharacteristics, EntityAttributeValueDto entityAttributeValue, Object entity) {
        if (!entityAttributeValue.getIsValueSet()) {
            return;
        }

        Method setterMethod = entityAttributeCharacteristics.getSetterMethod();
        Field attributeField = entityAttributeCharacteristics.getAttributeField();
        Method getterMethod = entityAttributeCharacteristics.getGetterMethod();

        final Map<String, String> valueToBeSet;

        try {

            if (entityAttributeValue.getShouldAppendCollection()) {

                Map<String, String> currentValue;

                if (getterMethod != null) {
                    currentValue = (Map<String, String>) getterMethod.invoke(entity);
                } else {
                    currentValue = (Map<String, String>) attributeField.get(entity);
                }

                if (currentValue == null) {
                    valueToBeSet = entityAttributeValue.getStringValueMap();
                } else {
                    Map<String, String> newValue = new HashMap<>();
                    newValue.putAll(currentValue);
                    newValue.putAll(entityAttributeValue.getStringValueMap());

                    valueToBeSet = newValue;
                }
            } else {
                valueToBeSet = entityAttributeValue.getStringValueMap();
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
        throw new RuntimeException("Searching for an entity by a String,String map as a parameter is not supported.");
    }

    @Override
    public boolean matchesEntityAttribute(EntityAttributeCharacteristicsDto entityAttributeCharacteristics) {
        return entityAttributeCharacteristics.getSetterType().getTypeName().equals("java.util.Map<java.lang.String, java.lang.String>");

    }

    @Override
    public BigDecimal getPriority() {
        return new BigDecimal(10);
    }
}
