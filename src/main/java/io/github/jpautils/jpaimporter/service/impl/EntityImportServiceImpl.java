package io.github.jpautils.jpaimporter.service.impl;

import io.github.jpautils.jpaimporter.dao.GenericJpaDao;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeDto;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeValueDto;
import io.github.jpautils.jpaimporter.dto.parsing.ParsedAttributeValuesRowDto;
import io.github.jpautils.jpaimporter.dto.parsing.ParsedEntityAttributeDto;
import io.github.jpautils.jpaimporter.dto.parsing.ParsedEntityImportBatchDto;
import io.github.jpautils.jpaimporter.enumeration.ImportOperation;
import io.github.jpautils.jpaimporter.exception.JpaImporterException;
import io.github.jpautils.jpaimporter.service.EntityAttributeService;
import io.github.jpautils.jpaimporter.service.EntityClassFinderService;
import io.github.jpautils.jpaimporter.service.EntityImportService;
import io.github.jpautils.jpaimporter.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EntityImportServiceImpl implements EntityImportService {

    private GenericJpaDao genericJpaDao;

    private EntityClassFinderService entityClassFinderService;

    private EntityAttributeService entityAttributeService;

    public EntityImportServiceImpl(
            GenericJpaDao genericJpaDao,
            EntityClassFinderService entityClassFinderService,
            EntityAttributeService entityAttributeService
    ) {
        this.genericJpaDao = genericJpaDao;
        this.entityClassFinderService = entityClassFinderService;
        this.entityAttributeService = entityAttributeService;
    }

    @Override
    public void importEntities(ParsedEntityImportBatchDto entityImportBatch, Map<String, Object> context) {
        try {
            importBatch(entityImportBatch, context);
        } catch (Exception exception) {
            //TODO: Provide the line number that was processed when the exception occurred.
            throw new JpaImporterException(
                    "Error occurred when importing batch. " +
                    "Operation: [" + entityImportBatch.getImportOperation() + "]. " +
                    "Entity: [" + entityImportBatch.getEntityName() + "]. " +
                    "Fields: [" + entityImportBatch.getEntityHeader().getEntityAttributes().stream().map(ParsedEntityAttributeDto::getAttributeName).collect(Collectors.toList()) + "]",
                    exception
            );
        }
    }

    private void importBatch(ParsedEntityImportBatchDto entityImportBatch, Map<String, Object> context) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Class<?> entityClass = entityClassFinderService.getEntityClassForName(entityImportBatch.getEntityName());
        List<EntityAttributeDto> entityAttributes = entityAttributeService.getEntityAttributesFor(entityImportBatch.getEntityHeader().getEntityAttributes(), entityClass);
        boolean shouldKeepEntityReferenceAfterOperation = entityImportBatch.getEntityHeader().getShouldKeepEntityReferenceAfterOperation();

        for (ParsedAttributeValuesRowDto parsedAttributeValuesRow : entityImportBatch.getEntityValuesRows()) {

            List<EntityAttributeValueDto> entityAttributeValues = entityAttributeService.getEntityAttributeValuesFor(entityAttributes, parsedAttributeValuesRow.getAttributeValues(), context);

            if (entityImportBatch.getImportOperation() == ImportOperation.INSERT) {
                Object createdEntity = createEntity(entityClass, entityAttributes, entityAttributeValues);
                storeEntityReferenceInContextIfNeeded(parsedAttributeValuesRow.getEntityVariableNameForStoringInContext(), shouldKeepEntityReferenceAfterOperation, context, createdEntity);
            }

            if (entityImportBatch.getImportOperation() == ImportOperation.UPSERT) {
                Optional<?> existentEntity = genericJpaDao.findExistentEntityBy(entityClass, entityAttributes, entityAttributeValues);

                if (existentEntity.isEmpty()) {
                    Object createdEntity = createEntity(entityClass, entityAttributes, entityAttributeValues);
                    storeEntityReferenceInContextIfNeeded(parsedAttributeValuesRow.getEntityVariableNameForStoringInContext(), shouldKeepEntityReferenceAfterOperation, context, createdEntity);
                } else {
                    Object existentEntityToBeUpdated = existentEntity.get();
                    Object updatedEntity = updateEntity(existentEntityToBeUpdated, entityAttributes, entityAttributeValues);
                    storeEntityReferenceInContextIfNeeded(parsedAttributeValuesRow.getEntityVariableNameForStoringInContext(), shouldKeepEntityReferenceAfterOperation, context, updatedEntity);
                }
            }

            if (entityImportBatch.getImportOperation() == ImportOperation.UPDATE) {
                Optional<?> existentEntity = genericJpaDao.findExistentEntityBy(entityClass, entityAttributes, entityAttributeValues);

                if (existentEntity.isEmpty()) {
                    throw new RuntimeException("Update failed because no existent entity found. Row values: [" + entityAttributeValues + "].");
                } else {
                    Object existentEntityToBeUpdated = existentEntity.get();
                    Object updatedEntity = updateEntity(existentEntityToBeUpdated, entityAttributes, entityAttributeValues);
                    storeEntityReferenceInContextIfNeeded(parsedAttributeValuesRow.getEntityVariableNameForStoringInContext(), shouldKeepEntityReferenceAfterOperation, context, updatedEntity);
                }
            }

            if (entityImportBatch.getImportOperation() == ImportOperation.DELETE) {
                Optional<?> existentEntity = genericJpaDao.findExistentEntityBy(entityClass, entityAttributes, entityAttributeValues);

                if (existentEntity.isEmpty()) {
                    throw new RuntimeException("Delete failed because no existent entity found. Row values: [" + entityAttributeValues + "].");
                } else {
                    genericJpaDao.delete(existentEntity.get());
                }
            }
        }
    }

    private void storeEntityReferenceInContextIfNeeded(String referenceName, boolean shouldStoreEntityReferenceInContext, Map<String, Object> context, Object entity) {
        if (
                shouldStoreEntityReferenceInContext
                && Util.stringIsNotBlank(referenceName)
                && referenceName.startsWith("@")
        ) {
            context.put(referenceName.toLowerCase(), entity);
        }
    }

    private Object createEntity(Class<?> entityClass, List<EntityAttributeDto> entityAttributes, List<EntityAttributeValueDto> entityAttributeValues) {
        Object instance = entityClassFinderService.createEntityInstanceForClass(entityClass);

        for (int i = 0; i < entityAttributes.size(); i++) {
            EntityAttributeDto entityAttribute = entityAttributes.get(i);
            EntityAttributeValueDto entityAttributeValue = entityAttributeValues.get(i);

            entityAttributeService.setEntityAttributeValue(instance, entityAttribute, entityAttributeValue);
        }

        return genericJpaDao.create(instance);
    }

    private Object updateEntity(Object existentEntityToBeUpdated, List<EntityAttributeDto> entityAttributes, List<EntityAttributeValueDto> entityAttributeValues) {
        for (int i = 0; i < entityAttributes.size(); i++) {
            EntityAttributeDto entityAttribute = entityAttributes.get(i);

            if (entityAttribute.getIsUnique() == false) {
                EntityAttributeValueDto entityAttributeValue = entityAttributeValues.get(i);
                entityAttributeService.setEntityAttributeValue(existentEntityToBeUpdated, entityAttribute, entityAttributeValue);
            }
        }

        return genericJpaDao.update(existentEntityToBeUpdated);
    }
}
