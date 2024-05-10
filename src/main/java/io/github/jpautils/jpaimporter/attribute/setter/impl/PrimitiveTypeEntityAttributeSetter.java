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

public class PrimitiveTypeEntityAttributeSetter implements EntityAttributeSetter {
    @Override
    public void setEntityAttributeValue(EntityAttributeCharacteristicsDto entityAttributeCharacteristics, EntityAttributeValueDto entityAttributeValue, Object entity) {
        if (!entityAttributeValue.getIsValueSet()) {
            return;
        }

        Method method = entityAttributeCharacteristics.getSetterMethod();
        Field field = entityAttributeCharacteristics.getAttributeField();
        String typeName = entityAttributeCharacteristics.getSetterType().getTypeName();
        String parsedValue = entityAttributeValue.getStringValue();

        try {
            if (method != null) {
                switch (typeName) {
                    case "boolean":
                        method.invoke(entity, Boolean.parseBoolean(parsedValue));
                        break;
                    case "byte":
                        method.invoke(entity, Byte.parseByte(parsedValue));
                        break;
                    case "char":
                        if (parsedValue.length() != 1) {
                            throw new RuntimeException("Could not convert string value: \"" + parsedValue + "\" to char because length is: [" + parsedValue.length() + "] but it should be 1");
                        }
                        method.invoke(entity, parsedValue.charAt(0));
                        break;
                    case "short":
                        method.invoke(entity, Short.parseShort(parsedValue));
                        break;
                    case "int":
                        method.invoke(entity, Integer.parseInt(parsedValue));
                        break;
                    case "long":
                        method.invoke(entity, Long.parseLong(parsedValue));
                        break;
                    case "float":
                        method.invoke(entity, Float.parseFloat(parsedValue));
                        break;
                    case "double":
                        method.invoke(entity, Double.parseDouble(parsedValue));
                        break;
                    default:
                        throw new RuntimeException("Could not set the attribute value because PrimitiveTypeEntityAttributeSetter expects the setter method type to be boolean/byte/char/short/int/long/float/double but got: [" + typeName + "].");
                }
            } else if (field != null) {
                switch (typeName) {
                    case "boolean":
                        field.setBoolean(entity, Boolean.parseBoolean(parsedValue));
                        break;
                    case "byte":
                        field.setByte(entity, Byte.parseByte(parsedValue));
                        break;
                    case "char":
                        if (parsedValue.length() != 1) {
                            throw new RuntimeException("Could not convert string value: \"" + parsedValue + "\" to char because length is: [" + parsedValue.length() + "] but it should be 1");
                        }
                        field.setChar(entity, parsedValue.charAt(0));
                        break;
                    case "short":
                        field.setShort(entity, Short.parseShort(parsedValue));
                        break;
                    case "int":
                        field.setInt(entity, Integer.parseInt(parsedValue));
                        break;
                    case "long":
                        field.setLong(entity, Long.parseLong(parsedValue));
                        break;
                    case "float":
                        field.setFloat(entity, Float.parseFloat(parsedValue));
                        break;
                    case "double":
                        field.setDouble(entity, Double.parseDouble(parsedValue));
                        break;
                    default:
                        throw new RuntimeException("Could not set the attribute value because PrimitiveTypeEntityAttributeSetter expects the field type should be boolean/byte/char/short/int/long/float/double but it was: [" + typeName + "].");
                }
            } else {
                throw new RuntimeException("Could not find a setter method/field for attribute: " + entityAttributeCharacteristics.getAttributeField().getName() + "and class: " + entityAttributeCharacteristics.getEntityClass());
            }
        } catch (Exception exception) {
            throw new RuntimeException("Could not set value for attribute [" + entityAttributeCharacteristics.getAttributeField().getName() + "] of class [" + entityAttributeCharacteristics.getEntityClass() + "]. Value: " + entityAttributeValue, exception);
        }
    }

    @Override
    public Predicate insertAttributeInCriteriaQuerySearch(EntityAttributeCharacteristicsDto entityAttributeCharacteristicsDto, EntityAttributeValueDto entityAttributeValueDto, String fieldName, CriteriaBuilder criteriaBuilder, Root<?> root) {
        if (entityAttributeValueDto.getStringValue() == null) {
            throw new RuntimeException("Cannot translate null to primitive value");
        }

        String typeName = entityAttributeCharacteristicsDto.getSetterType().getTypeName();
        String parsedValue = entityAttributeValueDto.getStringValue();

        switch (typeName) {
            case "boolean":
                return criteriaBuilder.equal(root.get(fieldName), Boolean.parseBoolean(parsedValue));
            case "byte":
                return criteriaBuilder.equal(root.get(fieldName), Byte.parseByte(parsedValue));
            case "char":
                if (parsedValue.length() != 1) {
                    throw new RuntimeException("Could not convert string value: \"" + parsedValue + "\" to char because length is: [" + parsedValue.length() + "] but it should be 1");
                }
                return criteriaBuilder.equal(root.get(fieldName), parsedValue.charAt(0));
            case "short":
                return criteriaBuilder.equal(root.get(fieldName), Short.parseShort(parsedValue));
            case "int":
                return criteriaBuilder.equal(root.get(fieldName), Integer.parseInt(parsedValue));
            case "long":
                return criteriaBuilder.equal(root.get(fieldName), Long.parseLong(parsedValue));
            case "float":
                return criteriaBuilder.equal(root.get(fieldName), Float.parseFloat(parsedValue));
            case "double":
                return criteriaBuilder.equal(root.get(fieldName), Double.parseDouble(parsedValue));
        }

        throw new RuntimeException("Could not match primitive type. Expected boolean/byte/char/short/int/long/float/double but got: [" + typeName + "].");
    }

    @Override
    public boolean matchesEntityAttribute(EntityAttributeCharacteristicsDto entityAttributeCharacteristicsDto) {
        String typeName = entityAttributeCharacteristicsDto.getSetterType().getTypeName();

        return typeName.equals("boolean") ||
                typeName.equals("byte") ||
                typeName.equals("char") ||
                typeName.equals("short") ||
                typeName.equals("int") ||
                typeName.equals("long") ||
                typeName.equals("float") ||
                typeName.equals("double");
    }

    @Override
    public BigDecimal getPriority() {
        return new BigDecimal(10);
    }
}
