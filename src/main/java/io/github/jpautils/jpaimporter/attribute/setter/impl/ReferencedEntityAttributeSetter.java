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
import java.math.BigDecimal;

public class ReferencedEntityAttributeSetter implements EntityAttributeSetter {
    @Override
    public void setEntityAttributeValue(EntityAttributeCharacteristicsDto entityAttributeCharacteristics, EntityAttributeValueDto entityAttributeValue, Object entity) {
        if (!entityAttributeValue.getIsValueSet()) {
            return;
        }

        Method method = entityAttributeCharacteristics.getSetterMethod();
        Field field = entityAttributeCharacteristics.getAttributeField();

        final Object valueToBeSet = entityAttributeValue.getEntityValue();

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
        return criteriaBuilder.equal(root.get(fieldName), entityAttributeValue.getEntityValue());
    }

    @Override
    public boolean matchesEntityAttribute(EntityAttributeCharacteristicsDto entityAttributeCharacteristics) {
        if (entityAttributeCharacteristics.getSetterType() instanceof Class) {
            Class<?> clazz = (Class<?>) entityAttributeCharacteristics.getSetterType();
            if (clazz.getAnnotation(Entity.class) != null || clazz.getAnnotation(MappedSuperclass.class) != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public BigDecimal getPriority() {
        return new BigDecimal(10);
    }
}
