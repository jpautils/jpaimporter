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
import java.util.Collection;
import java.util.HashSet;

public class StringListSetCollectionEntityAttributeSetter implements EntityAttributeSetter {
    @Override
    public void setEntityAttributeValue(EntityAttributeCharacteristicsDto entityAttributeCharacteristics, EntityAttributeValueDto entityAttributeValue, Object entity) {
        if (!entityAttributeValue.getIsValueSet()) {
            return;
        }

        Method method = entityAttributeCharacteristics.getSetterMethod();
        Field field = entityAttributeCharacteristics.getAttributeField();
        String setterType = entityAttributeCharacteristics.getSetterType().getTypeName();

        try {
            final Object valueToBeSet;

            if (setterType.equals("java.util.List<java.lang.String>")) {
                valueToBeSet = entityAttributeValue.getStringValueList();
            } else if (setterType.equals("java.util.Collection<java.lang.String>")) {
                valueToBeSet = (Collection<String>) entityAttributeValue.getStringValueList();
            } else if (setterType.equals("java.util.Set<java.lang.String>")) {
                valueToBeSet = new HashSet<String>(entityAttributeValue.getStringValueList());
            } else {
                throw new RuntimeException("Setter type: [" + setterType + "] is not supported, only List, Collection, Set of Strings are supported.");
            }

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
        throw new RuntimeException("Searching for an entity by a String list as a parameter is not supported.");
    }

    @Override
    public boolean matchesEntityAttribute(EntityAttributeCharacteristicsDto entityAttributeCharacteristics) {
        String setterType = entityAttributeCharacteristics.getSetterType().getTypeName();
        return setterType.equals("java.util.List<java.lang.String>") ||
                setterType.equals("java.util.Collection<java.lang.String>") ||
                setterType.equals("java.util.Set<java.lang.String>");
    }

    @Override
    public BigDecimal getPriority() {
        return new BigDecimal(10);
    }
}
