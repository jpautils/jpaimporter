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
import java.math.BigInteger;

public class BasicTypeEntityAttributeSetter implements EntityAttributeSetter {
    @Override
    public void setEntityAttributeValue(EntityAttributeCharacteristicsDto entityAttributeCharacteristics, EntityAttributeValueDto entityAttributeValue, Object entity) {
        if (!entityAttributeValue.getIsValueSet()) {
            return;
        }

        Method method = entityAttributeCharacteristics.getSetterMethod();
        Field field = entityAttributeCharacteristics.getAttributeField();

        final Object valueToBeSet = getValueToBeSet(entityAttributeCharacteristics, entityAttributeValue);

        try {
            if (method != null) {
                method.invoke(entity, valueToBeSet);
            } else if (field != null) {
                field.set(entity, valueToBeSet);
            } else {
                throw new RuntimeException("Could not find a setter method/field for attribute: " + entityAttributeCharacteristics.getAttributeField().getName() + "and class: " + entityAttributeCharacteristics.getEntityClass());
            }
        } catch (Exception exception) {
            throw new RuntimeException("Could not set value for attribute [" + entityAttributeCharacteristics.getAttributeField().getName() + "] of class [" + entityAttributeCharacteristics.getEntityClass() + "]. Value: " + entityAttributeValue, exception);
        }
    }

    @Override
    public Predicate insertAttributeInCriteriaQuerySearch(EntityAttributeCharacteristicsDto entityAttributeCharacteristics, EntityAttributeValueDto entityAttributeValue, String fieldName, CriteriaBuilder criteriaBuilder, Root<?> root) {
        Object valueToBeSet = getValueToBeSet(entityAttributeCharacteristics, entityAttributeValue);
        return criteriaBuilder.equal(root.get(fieldName), valueToBeSet);
    }

    @Override
    public boolean matchesEntityAttribute(EntityAttributeCharacteristicsDto entityAttributeCharacteristics) {
        String typeName = entityAttributeCharacteristics.getSetterType().getTypeName();
        return typeName.equals("java.lang.Boolean") ||
                typeName.equals("java.lang.Byte") ||
                typeName.equals("java.lang.Short") ||
                typeName.equals("java.lang.Character") ||
                typeName.equals("java.lang.Integer") ||
                typeName.equals("java.lang.Long") ||
                typeName.equals("java.lang.Float") ||
                typeName.equals("java.lang.Double") ||
                typeName.equals("java.lang.String") ||
                typeName.equals("java.math.BigInteger") ||
                typeName.equals("java.math.BigDecimal");
    }

    @Override
    public BigDecimal getPriority() {
        return new BigDecimal(10);
    }

    private Object getValueToBeSet(EntityAttributeCharacteristicsDto entityAttributeCharacteristics, EntityAttributeValueDto entityAttributeValue) {
        if (entityAttributeValue.getStringValue() == null) {
            return null;
        }

        String typeName = entityAttributeCharacteristics.getSetterType().getTypeName();
        String parsedValue = entityAttributeValue.getStringValue();

        switch (typeName) {
            case "java.lang.Boolean":
                return Boolean.valueOf(parsedValue);
            case "java.lang.Byte":
                return Byte.valueOf(parsedValue);
            case "java.lang.Short":
                return Short.valueOf(parsedValue);
            case "java.lang.Character":
                if (parsedValue.length() != 1) {
                    throw new RuntimeException("Could not convert string value: \"" + parsedValue + "\" to Character because length is: [" + parsedValue.length() + "] but it should be 1");
                }
                return parsedValue.charAt(0);
            case "java.lang.Integer":
                return Integer.valueOf(parsedValue);
            case "java.lang.Long":
                return Long.valueOf(parsedValue);
            case "java.lang.Float":
                return Float.valueOf(parsedValue);
            case "java.lang.Double":
                return Double.valueOf(parsedValue);
            case "java.lang.String":
                return parsedValue;
            case "java.math.BigInteger":
                return new BigInteger(parsedValue);
            case "java.math.BigDecimal":
                return new BigDecimal(parsedValue);
        }

        throw new RuntimeException("Could not match basic type. Expected java.lang.Boolean/Byte/Short/Character/Integer/Long/Float/Double/String/java.math.BigInteger/java.math.BigDecimal but got: [" + typeName + "].");
    }
}
