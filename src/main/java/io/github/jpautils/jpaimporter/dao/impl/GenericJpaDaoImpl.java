package io.github.jpautils.jpaimporter.dao.impl;

import io.github.jpautils.jpaimporter.attribute.setter.EntityAttributeSetter;
import io.github.jpautils.jpaimporter.dao.GenericJpaDao;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeDto;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeCharacteristicsDto;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeValueDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;

public class GenericJpaDaoImpl implements GenericJpaDao {

    private final EntityManager entityManager;

    public GenericJpaDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public <T> T create(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public <T> T update(T entity) {
        T updatedEntity = entityManager.merge(entity);
        return updatedEntity;
    }

    @Override
    public <T> void delete(T entity) {
        entityManager.remove(entity);
    }

    @Override
    public <T> Optional<T> findReferencedEntityBy(
            Class<T> entityClass,
            List<String> attributeNames,
            List<EntityAttributeSetter> attributeSetters,
            List<EntityAttributeCharacteristicsDto> attributesCharacteristics,
            List<?> attributeValues
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<T> root = criteriaQuery.from(entityClass);
        criteriaQuery.select(root);

        for (int i = 0; i < attributeSetters.size(); i++) {
            EntityAttributeSetter entityAttributeSetter = attributeSetters.get(i);
            EntityAttributeCharacteristicsDto entityAttributeCharacteristics = attributesCharacteristics.get(i);
            Object value = attributeValues.get(i);
            String attributeName = attributeNames.get(i);

            final EntityAttributeValueDto entityAttributeValue;

            if (value instanceof String) {
                entityAttributeValue = new EntityAttributeValueDto(
                        true,
                        false,
                        (String) value,
                        null,
                        null,
                        null,
                        null,
                        null
                );
            } else {
                entityAttributeValue = new EntityAttributeValueDto(
                        true,
                        false,
                        null,
                        null,
                        null,
                        value,
                        null,
                        null
                );
            }

            Predicate predicate = entityAttributeSetter.insertAttributeInCriteriaQuerySearch(entityAttributeCharacteristics, entityAttributeValue, attributeName, criteriaBuilder, root);

            criteriaQuery.where(predicate);
        }

        return entityManager.createQuery(criteriaQuery).getResultList().stream().findFirst();
    }

    @Override
    public <T> Optional<T> findExistentEntityBy(
            Class<T> entityClass,
            List<EntityAttributeDto> entityAttributes,
            List<EntityAttributeValueDto> entityAttributeValues
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<T> root = criteriaQuery.from(entityClass);
        criteriaQuery.select(root);

        for (int i = 0; i < entityAttributes.size(); i++) {
            EntityAttributeDto entityAttribute = entityAttributes.get(i);

            if (entityAttribute.getIsUnique()) {
                EntityAttributeValueDto entityAttributeValue = entityAttributeValues.get(i);
                EntityAttributeSetter entityAttributeSetter = entityAttribute.getEntityAttributeSetter();
                EntityAttributeCharacteristicsDto entityAttributeSetterCharacteristics = entityAttribute.getEntityAttributeCharacteristics();


                Predicate predicate = entityAttributeSetter.insertAttributeInCriteriaQuerySearch(entityAttributeSetterCharacteristics, entityAttributeValue, entityAttribute.getAttributeName(), criteriaBuilder, root);

                criteriaQuery.where(predicate);
            }
        }

        return entityManager.createQuery(criteriaQuery).getResultList().stream().findFirst();
    }
}
