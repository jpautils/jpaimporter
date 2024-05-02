package io.github.jpautils.jpaimporter.service.impl


import io.github.jpautils.jpaimporter.dao.GenericJpaDao
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeDto
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeValueDto
import io.github.jpautils.jpaimporter.dto.parsing.*
import io.github.jpautils.jpaimporter.enumeration.ImportOperation
import io.github.jpautils.jpaimporter.service.EntityAttributeService
import io.github.jpautils.jpaimporter.service.EntityClassFinderService
import io.github.jpautils.jpaimporter.testingDummyClass.TestingDummyClass
import spock.lang.Specification

class EntityImportServiceImplTest extends Specification {
    GenericJpaDao genericJpaDaoMock = Mock()
    EntityClassFinderService entityClassFinderServiceMock = Mock()
    EntityAttributeService entityAttributeServiceMock = Mock()

    EntityImportServiceImpl entityImportService = new EntityImportServiceImpl(genericJpaDaoMock, entityClassFinderServiceMock, entityAttributeServiceMock)

    def "should create entity for import operation INSERT"() {
        Class<?> entityClass = TestingDummyClass

        ParsedEntityAttributeDto parsedAttribute1 = Mock()
        ParsedEntityAttributeDto parsedAttribute2 = Mock()
        ParsedAttributeValueDto parsedValueAttribute1Row1 = Mock()
        ParsedAttributeValueDto parsedValueAttribute2Row1 = Mock()

        EntityAttributeDto entityAttribute1 = Mock()
        EntityAttributeDto entityAttribute2 = Mock()
        EntityAttributeValueDto entityValueAttribute1Row1 = Mock()
        EntityAttributeValueDto entityValueAttribute2Row1 = Mock()

        ParsedEntityImportBatchDto parsedEntityImportBatchDto = new ParsedEntityImportBatchDto(
                ImportOperation.INSERT,
                'EntityName',
                new ParsedEntityHeaderDto([parsedAttribute1, parsedAttribute2], false, null),
                [new ParsedAttributeValuesRowDto([parsedValueAttribute1Row1, parsedValueAttribute2Row1], null)]
        )
        Map<String, Object> context = new HashMap<>()
        Object createdEntity = Mock()

        when:
        entityImportService.importEntities(parsedEntityImportBatchDto, context)

        then:
        1 * entityClassFinderServiceMock.getEntityClassForName('EntityName') >> entityClass
        1 * entityAttributeServiceMock.getEntityAttributesFor([parsedAttribute1, parsedAttribute2], entityClass) >> [entityAttribute1, entityAttribute2]
        1 * entityAttributeServiceMock.getEntityAttributeValuesFor([entityAttribute1, entityAttribute2], [parsedValueAttribute1Row1, parsedValueAttribute2Row1], context) >> [entityValueAttribute1Row1, entityValueAttribute2Row1]
        1 * entityClassFinderServiceMock.createEntityInstanceForClass(entityClass) >> createdEntity
        1 * entityAttributeServiceMock.setEntityAttributeValue(createdEntity, entityAttribute1, entityValueAttribute1Row1)
        1 * entityAttributeServiceMock.setEntityAttributeValue(createdEntity, entityAttribute2, entityValueAttribute2Row1)
        1 * genericJpaDaoMock.create(createdEntity)
        0 * _
    }

    def "should create entity for import operation UPSERT and no existent entity"() {
        Class<?> entityClass = TestingDummyClass

        ParsedEntityAttributeDto parsedAttribute1 = Mock()
        ParsedEntityAttributeDto parsedAttribute2 = Mock()
        ParsedAttributeValueDto parsedValueAttribute1Row1 = Mock()
        ParsedAttributeValueDto parsedValueAttribute2Row1 = Mock()

        EntityAttributeDto entityAttribute1 = Mock()
        EntityAttributeDto entityAttribute2 = Mock()
        EntityAttributeValueDto entityValueAttribute1Row1 = Mock()
        EntityAttributeValueDto entityValueAttribute2Row1 = Mock()

        ParsedEntityImportBatchDto parsedEntityImportBatchDto = new ParsedEntityImportBatchDto(
                ImportOperation.UPSERT,
                'EntityName',
                new ParsedEntityHeaderDto([parsedAttribute1, parsedAttribute2], false, null),
                [new ParsedAttributeValuesRowDto([parsedValueAttribute1Row1, parsedValueAttribute2Row1], null)]
        )
        Map<String, Object> context = new HashMap<>()
        Object createdEntity = Mock()

        when:
        entityImportService.importEntities(parsedEntityImportBatchDto, context)

        then:
        1 * entityClassFinderServiceMock.getEntityClassForName('EntityName') >> entityClass
        1 * entityAttributeServiceMock.getEntityAttributesFor([parsedAttribute1, parsedAttribute2], entityClass) >> [entityAttribute1, entityAttribute2]
        1 * entityAttributeServiceMock.getEntityAttributeValuesFor([entityAttribute1, entityAttribute2], [parsedValueAttribute1Row1, parsedValueAttribute2Row1], context) >> [entityValueAttribute1Row1, entityValueAttribute2Row1]
        1 * genericJpaDaoMock.findExistentEntityBy(entityClass, [entityAttribute1, entityAttribute2], [entityValueAttribute1Row1, entityValueAttribute2Row1]) >> Optional.empty()
        1 * entityClassFinderServiceMock.createEntityInstanceForClass(entityClass) >> createdEntity
        1 * entityAttributeServiceMock.setEntityAttributeValue(createdEntity, entityAttribute1, entityValueAttribute1Row1)
        1 * entityAttributeServiceMock.setEntityAttributeValue(createdEntity, entityAttribute2, entityValueAttribute2Row1)
        1 * genericJpaDaoMock.create(createdEntity)
        0 * _
    }

    def "should update entity for import operation UPSERT and found existent entity"() {
        Class<?> entityClass = TestingDummyClass

        ParsedEntityAttributeDto parsedAttribute1 = Mock()
        ParsedEntityAttributeDto parsedAttribute2 = Mock()
        ParsedAttributeValueDto parsedValueAttribute1Row1 = Mock()
        ParsedAttributeValueDto parsedValueAttribute2Row1 = Mock()

        EntityAttributeDto entityAttribute1 = Mock()
        EntityAttributeDto entityAttribute2 = Mock()
        EntityAttributeValueDto entityValueAttribute1Row1 = Mock()
        EntityAttributeValueDto entityValueAttribute2Row1 = Mock()

        ParsedEntityImportBatchDto parsedEntityImportBatchDto = new ParsedEntityImportBatchDto(
                ImportOperation.UPSERT,
                'EntityName',
                new ParsedEntityHeaderDto([parsedAttribute1, parsedAttribute2], false, null),
                [new ParsedAttributeValuesRowDto([parsedValueAttribute1Row1, parsedValueAttribute2Row1], null)]
        )
        Map<String, Object> context = new HashMap<>()
        Object foundEntity = Mock()

        when:
        entityImportService.importEntities(parsedEntityImportBatchDto, context)

        then:
        1 * entityClassFinderServiceMock.getEntityClassForName('EntityName') >> entityClass
        1 * entityAttributeServiceMock.getEntityAttributesFor([parsedAttribute1, parsedAttribute2], entityClass) >> [entityAttribute1, entityAttribute2]
        1 * entityAttributeServiceMock.getEntityAttributeValuesFor([entityAttribute1, entityAttribute2], [parsedValueAttribute1Row1, parsedValueAttribute2Row1], context) >> [entityValueAttribute1Row1, entityValueAttribute2Row1]
        1 * genericJpaDaoMock.findExistentEntityBy(entityClass, [entityAttribute1, entityAttribute2], [entityValueAttribute1Row1, entityValueAttribute2Row1]) >> Optional.of(foundEntity)
        1 * entityAttribute1.getIsUnique() >> true
        1 * entityAttribute2.getIsUnique() >> false
        1 * entityAttributeServiceMock.setEntityAttributeValue(foundEntity, entityAttribute2, entityValueAttribute2Row1)
        1 * genericJpaDaoMock.update(foundEntity)
        0 * _
    }

    def "should update entity for import operation UPDATE"() {
        Class<?> entityClass = TestingDummyClass

        ParsedEntityAttributeDto parsedAttribute1 = Mock()
        ParsedEntityAttributeDto parsedAttribute2 = Mock()
        ParsedAttributeValueDto parsedValueAttribute1Row1 = Mock()
        ParsedAttributeValueDto parsedValueAttribute2Row1 = Mock()

        EntityAttributeDto entityAttribute1 = Mock()
        EntityAttributeDto entityAttribute2 = Mock()
        EntityAttributeValueDto entityValueAttribute1Row1 = Mock()
        EntityAttributeValueDto entityValueAttribute2Row1 = Mock()

        ParsedEntityImportBatchDto parsedEntityImportBatchDto = new ParsedEntityImportBatchDto(
                ImportOperation.UPDATE,
                'EntityName',
                new ParsedEntityHeaderDto([parsedAttribute1, parsedAttribute2], false, null),
                [new ParsedAttributeValuesRowDto([parsedValueAttribute1Row1, parsedValueAttribute2Row1], null)]
        )
        Map<String, Object> context = new HashMap<>()
        Object foundEntity = Mock()

        when:
        entityImportService.importEntities(parsedEntityImportBatchDto, context)

        then:
        1 * entityClassFinderServiceMock.getEntityClassForName('EntityName') >> entityClass
        1 * entityAttributeServiceMock.getEntityAttributesFor([parsedAttribute1, parsedAttribute2], entityClass) >> [entityAttribute1, entityAttribute2]
        1 * entityAttributeServiceMock.getEntityAttributeValuesFor([entityAttribute1, entityAttribute2], [parsedValueAttribute1Row1, parsedValueAttribute2Row1], context) >> [entityValueAttribute1Row1, entityValueAttribute2Row1]
        1 * genericJpaDaoMock.findExistentEntityBy(entityClass, [entityAttribute1, entityAttribute2], [entityValueAttribute1Row1, entityValueAttribute2Row1]) >> Optional.of(foundEntity)
        1 * entityAttribute1.getIsUnique() >> true
        1 * entityAttribute2.getIsUnique() >> false
        1 * entityAttributeServiceMock.setEntityAttributeValue(foundEntity, entityAttribute2, entityValueAttribute2Row1)
        1 * genericJpaDaoMock.update(foundEntity)
        0 * _
    }

    def "should delete entity for import operation DELETE"() {
        Class<?> entityClass = TestingDummyClass

        ParsedEntityAttributeDto parsedAttribute1 = Mock()
        ParsedEntityAttributeDto parsedAttribute2 = Mock()
        ParsedAttributeValueDto parsedValueAttribute1Row1 = Mock()
        ParsedAttributeValueDto parsedValueAttribute2Row1 = Mock()

        EntityAttributeDto entityAttribute1 = Mock()
        EntityAttributeDto entityAttribute2 = Mock()
        EntityAttributeValueDto entityValueAttribute1Row1 = Mock()
        EntityAttributeValueDto entityValueAttribute2Row1 = Mock()

        ParsedEntityImportBatchDto parsedEntityImportBatchDto = new ParsedEntityImportBatchDto(
                ImportOperation.DELETE,
                'EntityName',
                new ParsedEntityHeaderDto([parsedAttribute1, parsedAttribute2], false, null),
                [new ParsedAttributeValuesRowDto([parsedValueAttribute1Row1, parsedValueAttribute2Row1], null)]
        )
        Map<String, Object> context = new HashMap<>()
        Object foundEntity = Mock()

        when:
        entityImportService.importEntities(parsedEntityImportBatchDto, context)

        then:
        1 * entityClassFinderServiceMock.getEntityClassForName('EntityName') >> entityClass
        1 * entityAttributeServiceMock.getEntityAttributesFor([parsedAttribute1, parsedAttribute2], entityClass) >> [entityAttribute1, entityAttribute2]
        1 * entityAttributeServiceMock.getEntityAttributeValuesFor([entityAttribute1, entityAttribute2], [parsedValueAttribute1Row1, parsedValueAttribute2Row1], context) >> [entityValueAttribute1Row1, entityValueAttribute2Row1]
        1 * genericJpaDaoMock.findExistentEntityBy(entityClass, [entityAttribute1, entityAttribute2], [entityValueAttribute1Row1, entityValueAttribute2Row1]) >> Optional.of(foundEntity)
        1 * genericJpaDaoMock.delete(foundEntity)
        0 * _
    }

    def "should process correctly multiple rows and store reference if needed"() {
        Class<?> entityClass = TestingDummyClass

        ParsedEntityAttributeDto parsedAttribute1 = Mock()
        ParsedEntityAttributeDto parsedAttribute2 = Mock()
        ParsedAttributeValueDto parsedValueAttribute1Row1 = Mock()
        ParsedAttributeValueDto parsedValueAttribute2Row1 = Mock()
        ParsedAttributeValueDto parsedValueAttribute1Row2 = Mock()
        ParsedAttributeValueDto parsedValueAttribute2Row2 = Mock()
        ParsedAttributeValueDto parsedValueAttribute1Row3 = Mock()
        ParsedAttributeValueDto parsedValueAttribute2Row3 = Mock()

        EntityAttributeDto entityAttribute1 = Mock()
        EntityAttributeDto entityAttribute2 = Mock()
        EntityAttributeValueDto entityValueAttribute1Row1 = Mock()
        EntityAttributeValueDto entityValueAttribute2Row1 = Mock()
        EntityAttributeValueDto entityValueAttribute1Row2 = Mock()
        EntityAttributeValueDto entityValueAttribute2Row2 = Mock()
        EntityAttributeValueDto entityValueAttribute1Row3 = Mock()
        EntityAttributeValueDto entityValueAttribute2Row3 = Mock()

        ParsedEntityImportBatchDto parsedEntityImportBatchDto = new ParsedEntityImportBatchDto(
                ImportOperation.UPSERT,
                'EntityName',
                new ParsedEntityHeaderDto([parsedAttribute1, parsedAttribute2], false, null),
                [
                        new ParsedAttributeValuesRowDto([parsedValueAttribute1Row1, parsedValueAttribute2Row1], "@refRow1"),
                        new ParsedAttributeValuesRowDto([parsedValueAttribute1Row2, parsedValueAttribute2Row2], "refRow2"),
                        new ParsedAttributeValuesRowDto([parsedValueAttribute1Row3, parsedValueAttribute2Row3], null)
                ]
        )
        Map<String, Object> context = new HashMap<>()
        Object foundEntityRow1 = Mock()
        Object createdEntityRow2 = Mock()
        Object foundEntityRow3 = Mock()

        when:
        entityImportService.importEntities(parsedEntityImportBatchDto, context)

        then:
        1 * entityClassFinderServiceMock.getEntityClassForName('EntityName') >> entityClass
        1 * entityAttributeServiceMock.getEntityAttributesFor([parsedAttribute1, parsedAttribute2], entityClass) >> [entityAttribute1, entityAttribute2]


        1 * entityAttributeServiceMock.getEntityAttributeValuesFor([entityAttribute1, entityAttribute2], [parsedValueAttribute1Row1, parsedValueAttribute2Row1], context) >> [entityValueAttribute1Row1, entityValueAttribute2Row1]
        1 * genericJpaDaoMock.findExistentEntityBy(entityClass, [entityAttribute1, entityAttribute2], [entityValueAttribute1Row1, entityValueAttribute2Row1]) >> Optional.of(foundEntityRow1)
        1 * entityAttribute1.getIsUnique() >> true
        1 * entityAttribute2.getIsUnique() >> false
        1 * entityAttributeServiceMock.setEntityAttributeValue(foundEntityRow1, entityAttribute2, entityValueAttribute2Row1)
        1 * genericJpaDaoMock.update(foundEntityRow1)

        1 * entityAttributeServiceMock.getEntityAttributeValuesFor([entityAttribute1, entityAttribute2], [parsedValueAttribute1Row2, parsedValueAttribute2Row2], context) >> [entityValueAttribute1Row2, entityValueAttribute2Row2]
        1 * genericJpaDaoMock.findExistentEntityBy(entityClass, [entityAttribute1, entityAttribute2], [entityValueAttribute1Row2, entityValueAttribute2Row2]) >> Optional.empty()
        1 * entityClassFinderServiceMock.createEntityInstanceForClass(entityClass) >> createdEntityRow2
        1 * entityAttributeServiceMock.setEntityAttributeValue(createdEntityRow2, entityAttribute1, entityValueAttribute1Row2)
        1 * entityAttributeServiceMock.setEntityAttributeValue(createdEntityRow2, entityAttribute2, entityValueAttribute2Row2)
        1 * genericJpaDaoMock.create(createdEntityRow2)

        1 * entityAttributeServiceMock.getEntityAttributeValuesFor([entityAttribute1, entityAttribute2], [parsedValueAttribute1Row3, parsedValueAttribute2Row3], context) >> [entityValueAttribute1Row3, entityValueAttribute2Row3]
        1 * genericJpaDaoMock.findExistentEntityBy(entityClass, [entityAttribute1, entityAttribute2], [entityValueAttribute1Row3, entityValueAttribute2Row3]) >> Optional.of(foundEntityRow3)
        1 * entityAttribute1.getIsUnique() >> true
        1 * entityAttribute2.getIsUnique() >> false
        1 * entityAttributeServiceMock.setEntityAttributeValue(foundEntityRow3, entityAttribute2, entityValueAttribute2Row3)
        1 * genericJpaDaoMock.update(foundEntityRow3)


        0 * _
    }
}
