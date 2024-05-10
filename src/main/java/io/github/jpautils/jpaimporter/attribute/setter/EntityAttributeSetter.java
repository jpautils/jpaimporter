package io.github.jpautils.jpaimporter.attribute.setter;

import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeCharacteristicsDto;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeValueDto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;


public interface EntityAttributeSetter {
    void setEntityAttributeValue(
            EntityAttributeCharacteristicsDto entityAttributeCharacteristics,
            EntityAttributeValueDto entityAttributeValue,
            Object entity
    );

    Predicate insertAttributeInCriteriaQuerySearch(
            EntityAttributeCharacteristicsDto entityAttributeCharacteristics,
            EntityAttributeValueDto entityAttributeValue,
            String fieldName,
            CriteriaBuilder criteriaBuilder,
            Root<?> root
    );

    boolean matchesEntityAttribute(EntityAttributeCharacteristicsDto entityAttributeCharacteristics);

    BigDecimal getPriority();
}
