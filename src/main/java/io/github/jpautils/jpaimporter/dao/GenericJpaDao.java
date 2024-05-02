package io.github.jpautils.jpaimporter.dao;

import io.github.jpautils.jpaimporter.attribute.setter.EntityAttributeSetter;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeDto;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeCharacteristicsDto;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeValueDto;

import java.util.List;
import java.util.Optional;

public interface GenericJpaDao {
    <T> T create(T entity);
    <T> T update(T entity);
    <T> void delete(T entity);

    <T> Optional<T> findReferencedEntityBy(
            Class<T> entityClass,
            List<String> attributeNames,
            List<EntityAttributeSetter> attributeSetters,
            List<EntityAttributeCharacteristicsDto> attributesCharacteristics,
            List<?> attributeValues
    );

    <T> Optional<T> findExistentEntityBy(
            Class<T> entityClass,
            List<EntityAttributeDto> entityAttributes,
            List<EntityAttributeValueDto> entityAttributeValues
    );
}
