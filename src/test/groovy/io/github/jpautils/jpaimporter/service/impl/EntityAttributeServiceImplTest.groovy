package io.github.jpautils.jpaimporter.service.impl

import io.github.jpautils.jpaimporter.attribute.chooser.EntityAttributeSetterChooser
import io.github.jpautils.jpaimporter.attribute.setter.EntityAttributeSetter
import io.github.jpautils.jpaimporter.dao.GenericJpaDao
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeDto
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeCharacteristicsDto
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeValueDto
import io.github.jpautils.jpaimporter.dto.parsing.ParsedAttributeValueDto
import io.github.jpautils.jpaimporter.dto.parsing.ParsedEntityAttributeDto
import io.github.jpautils.jpaimporter.testingDummyClass.TestingDummyClass
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Type

class EntityAttributeServiceImplTest extends Specification {
    GenericJpaDao genericJpaDaoMock = Mock()
    EntityAttributeSetterChooser entityAttributeSetterChooserMock = Mock()
    @Shared Object entityMock = Mock()
    @Shared Object referencedEntityMock = Mock()

    EntityAttributeServiceImpl entityAttributeService = Spy(constructorArgs: [genericJpaDaoMock, entityAttributeSetterChooserMock])

    def 'should get correct EntityAttributes for ParsedAttribute without referenced entity'() {
        Class<?> entityClass = TestingDummyClass
        ParsedEntityAttributeDto parsedAttribute = new ParsedEntityAttributeDto(attributeNameParam, isUniqueParam, attributeIsAMapValueForMapKeyNameParam, false, null)
        EntityAttributeCharacteristicsDto entityAttributeSetterCharacteristicsMock = Mock()
        EntityAttributeSetter entityAttributeSetterMock = Mock()

        when:
        EntityAttributeDto result = entityAttributeService.getEntityAttributesFor([parsedAttribute], entityClass)[0]

        then:
        1 * entityAttributeService.getEntityAttributeSetterCharacteristics(entityClass, attributeNameParam) >> entityAttributeSetterCharacteristicsMock
        1 * entityAttributeSetterChooserMock.getEntityAttributeSetterFor(entityAttributeSetterCharacteristicsMock) >> entityAttributeSetterMock
        result.attributeName == attributeNameParam
        result.isUnique == isUniqueParam
        result.attributeIsAMapValueForMapKeyName == attributeIsAMapValueForMapKeyNameParam
        result.entityAttributeSetter == entityAttributeSetterMock
        result.isAttributeReferencingEntity == false
        result.referencedEntityUniqueAttributes == null
        result.referencedEntityClass == null
        result.referencedEntityUniqueAttributesSetters == null

        where:
        attributeNameParam  | isUniqueParam | attributeIsAMapValueForMapKeyNameParam
        'attributeName'     | true          | 'key name for map value'
        'attributeName'     | false         | null
        'attributeName'     | true          | null
        'attributeName'     | false         | ''
    }

    def 'should get correct EntityAttributes for ParsedAttribute with referenced entity'() {
        Class<?> entityClass = TestingDummyClass
        ParsedEntityAttributeDto parsedAttribute = new ParsedEntityAttributeDto('attribute1', true, null, true, ['referencedUniqueAttribute1', 'referencedUniqueAttribute2'])
        EntityAttributeCharacteristicsDto attribute1CharacteristicsMock = Mock()
        EntityAttributeSetter attribute1SetterMock = Mock()

        EntityAttributeCharacteristicsDto referencedAttribute1CharacteristicsMock = Mock()
        EntityAttributeSetter referencedAttribute1SetterMock = Mock()

        EntityAttributeCharacteristicsDto referencedAttribute2CharacteristicsMock = Mock()
        EntityAttributeSetter referencedAttribute2SetterMock = Mock()

        Type referencedEntitySetterType = referencedEntitySetterTypeParam
        Class<?> resolvedReferencedEntityClass = resolvedReferencedEntityClassParam


        when:
        EntityAttributeDto result = entityAttributeService.getEntityAttributesFor([parsedAttribute], entityClass)[0]

        then:
        1 * entityAttributeService.getEntityAttributeSetterCharacteristics(entityClass, 'attribute1') >> attribute1CharacteristicsMock
        1 * entityAttributeSetterChooserMock.getEntityAttributeSetterFor(attribute1CharacteristicsMock) >> attribute1SetterMock
        1 * attribute1CharacteristicsMock.getSetterType() >> referencedEntitySetterType
        result.attributeName == 'attribute1'
        result.isUnique
        result.attributeIsAMapValueForMapKeyName == null
        result.entityAttributeSetter == attribute1SetterMock
        result.isAttributeReferencingEntity == true
        result.referencedEntityUniqueAttributes == ['referencedUniqueAttribute1', 'referencedUniqueAttribute2']
        result.referencedEntityClass == resolvedReferencedEntityClass
        1 * entityAttributeService.getEntityAttributeSetterCharacteristics(resolvedReferencedEntityClass, 'referencedUniqueAttribute1') >> referencedAttribute1CharacteristicsMock
        1 * entityAttributeSetterChooserMock.getEntityAttributeSetterFor(referencedAttribute1CharacteristicsMock) >> referencedAttribute1SetterMock
        1 * entityAttributeService.getEntityAttributeSetterCharacteristics(resolvedReferencedEntityClass, 'referencedUniqueAttribute2') >> referencedAttribute2CharacteristicsMock
        1 * entityAttributeSetterChooserMock.getEntityAttributeSetterFor(referencedAttribute2CharacteristicsMock) >> referencedAttribute2SetterMock
        result.referencedEntityUniqueAttributesSetters == [referencedAttribute1SetterMock, referencedAttribute2SetterMock]

        where:
        referencedEntitySetterTypeParam                                                                 | resolvedReferencedEntityClassParam    | expectedTypeNameParam
        TestingDummyClass.getField('parametrizedTypeWithNoGetSetMethods').getAnnotatedType().getType()  | String.class                          | ''
        TestingDummyClass.getField('typeWithNoGetSetMethods').getAnnotatedType().getType()              | String.class                          | ''
    }


    def 'should get correct EntityAttributeSetterCharacteristics'() {
        given:
        Class<?> entityClass = TestingDummyClass.class

        when:
        EntityAttributeCharacteristicsDto result = entityAttributeService.getEntityAttributeSetterCharacteristics(entityClass, attributeNameParam)

        then:
        result.attributeField.name == attributeNameParam
        result.getterMethod?.name == getterMethodNameParam
        result.setterMethod?.name == setterMethodNameParam
        result.getterType?.typeName == getterTypeParam
        result.setterType?.typeName == setterTypeParam
        result.entityClass == entityClass

        where:
        attributeNameParam                      | getterMethodNameParam                     | setterMethodNameParam                     | getterTypeParam                       | setterTypeParam
        'parametrizedTypeWithNoGetSetMethods'   | null                                      | null                                      | 'java.util.List<java.lang.String>'    | 'java.util.List<java.lang.String>'
        'parametrizedTypeWithGetMethod'         | 'getParametrizedTypeWithGetMethod'        | null                                      | 'java.util.List<java.lang.String>'    | 'java.util.List<java.lang.String>'
        'parametrizedTypeWithSetMethod'         | null                                      | 'setParametrizedTypeWithSetMethod'        | 'java.util.List<java.lang.String>'    | 'java.util.List<java.lang.String>'
        'parametrizedTypeWithGetSetMethods'     | 'getParametrizedTypeWithGetSetMethods'    | 'setParametrizedTypeWithGetSetMethods'    | 'java.util.List<java.lang.String>'    | 'java.util.List<java.lang.String>'
        'typeWithNoGetSetMethods'               | null                                      | null                                      | 'java.lang.String'                    | 'java.lang.String'
        'typeWithGetMethod'                     | 'getTypeWithGetMethod'                    | null                                      | 'java.lang.String'                    | 'java.lang.String'
        'typeWithSetMethod'                     | null                                      | 'setTypeWithSetMethod'                    | 'java.lang.String'                    | 'java.lang.String'
        'typeWithGetSetMethods'                 | 'getTypeWithGetSetMethods'                | 'setTypeWithGetSetMethods'                | 'java.lang.String'                    | 'java.lang.String'
    }

    def 'should set the entity attribute value using the EntityAttributeSetter'() {
        Object instanceMock = Mock()
        EntityAttributeDto entityAttributeMock = Mock()
        EntityAttributeValueDto entityAttributeValueMock = Mock()
        EntityAttributeSetter entityAttributeSetterMock = Mock()
        EntityAttributeCharacteristicsDto entityAttributeCharacteristicsMock = Mock()

        when:
        entityAttributeService.setEntityAttributeValue(instanceMock, entityAttributeMock, entityAttributeValueMock)

        then:
        1 * entityAttributeService.setEntityAttributeValue(instanceMock, entityAttributeMock, entityAttributeValueMock)
        1 * entityAttributeMock.getEntityAttributeSetter() >> entityAttributeSetterMock
        1 * entityAttributeMock.getEntityAttributeCharacteristics() >> entityAttributeCharacteristicsMock
        1 * entityAttributeSetterMock.setEntityAttributeValue(entityAttributeCharacteristicsMock, entityAttributeValueMock, instanceMock)
        0 * _
    }

    def 'should return correct EntityAttributeValueDto when #testDescription'() {
        given:
        EntityAttributeSetter entityAttributeSetterMock = Mock()
        EntityAttributeCharacteristicsDto entityAttributeCharacteristicsMock = Mock()

        EntityAttributeDto entityAttribute = new EntityAttributeDto(
                'attributeName',
                true,
                attributeIsAMapValueForMapKeyName,
                entityAttributeSetterMock,
                entityAttributeCharacteristicsMock,
                false,
                null,
                null,
                null,
                null
        )

        ParsedAttributeValueDto parsedAttributeValue = new ParsedAttributeValueDto(
                parsedValue,
                parsedValueList,
                parsedValueMap
        )
        Map<String, Object> context = ['@ref': entityMock]

        when:
        EntityAttributeValueDto result = entityAttributeService.getEntityAttributeValuesFor([entityAttribute], [parsedAttributeValue], context)[0]

        then:
        result.getIsValueSet() == isValueSet
        result.getShouldAppendCollection() == shouldAppendCollection
        result.getStringValue() == stringValue
        result.getStringValueList() == stringValueList
        result.getStringValueMap() == stringValueMap
        result.getEntityValue() == entityValue
        result.getEntityValueList() == entityValueList
        result.getEntityValueMap() == entityValueMap

        where:
        parsedValue | parsedValueList   | parsedValueMap                    | attributeIsAMapValueForMapKeyName | isValueSet    | shouldAppendCollection    | stringValue   | stringValueList   | stringValueMap                        | entityValue   | entityValueList           | entityValueMap                        | testDescription
        ''          | null              | null                              | null                              | false         | false                     | null          | null              | null                                  | null          | null                      | null                                  | 'parsed value is empty'
        '    '      | null              | null                              | null                              | false         | false                     | null          | null              | null                                  | null          | null                      | null                                  | 'parsed value is blank'
        ''          | null              | null                              | 'keyName'                         | false         | false                     | null          | null              | null                                  | null          | null                      | null                                  | 'parsed value is empty and attribute is a map value for key name'
        ' '         | null              | null                              | 'keyName'                         | false         | false                     | null          | null              | null                                  | null          | null                      | null                                  | 'parsed value is blank and attribute is a map value for key name'
        '""'        | null              | null                              | null                              | true          | false                     | ''            | null              | null                                  | null          | null                      | null                                  | 'parsed value is empty but double quotes are present'
        '" "'       | null              | null                              | null                              | true          | false                     | ' '           | null              | null                                  | null          | null                      | null                                  | 'parsed value is blank but double quotes are present'
        ' "" '      | null              | null                              | null                              | true          | false                     | ''            | null              | null                                  | null          | null                      | null                                  | 'parsed value is empty but double quotes are present with blank space before and after'
        ' " " '     | null              | null                              | null                              | true          | false                     | ' '           | null              | null                                  | null          | null                      | null                                  | 'parsed value is blank but double quotes are present with blank space before and after'
        'null'      | null              | null                              | null                              | true          | false                     | null          | null              | null                                  | null          | null                      | null                                  | 'parsed value is set to keyword null'
        ' null '    | null              | null                              | null                              | true          | false                     | null          | null              | null                                  | null          | null                      | null                                  | 'parsed value is set to keyword null with blank space before and after'
        '"null"'    | null              | null                              | null                              | true          | false                     | "null"        | null              | null                                  | null          | null                      | null                                  | 'parsed value is set to text null between double quotes'
        '" null "'  | null              | null                              | null                              | true          | false                     | " null "      | null              | null                                  | null          | null                      | null                                  | 'parsed value is set to text null with blank space between double quotes'
        '""'        | null              | null                              | 'keyName'                         | true          | true                      | null          | null              | ['keyName': '']                       | null          | null                      | null                                  | 'parsed value is set to double quotes and attribute is a map value'
        '" "'       | null              | null                              | 'keyName'                         | true          | true                      | null          | null              | ['keyName': ' ']                      | null          | null                      | null                                  | 'parsed value is set to double quotes with blank space inside and attribute is a map value'
        'null'      | null              | null                              | 'keyName'                         | true          | true                      | null          | null              | ['keyName': null]                     | null          | null                      | ['keyName': null]                     | 'parsed value is set to null keyword and attribute is a map value'
        '  null  '  | null              | null                              | 'keyName'                         | true          | true                      | null          | null              | ['keyName': null]                     | null          | null                      | ['keyName': null]                     | 'parsed value is set to null keyword with blank space and attribute is a map value'
        '"null"'    | null              | null                              | 'keyName'                         | true          | true                      | null          | null              | ['keyName': 'null']                   | null          | null                      | null                                  | 'parsed value is set to null text between double quotes and attribute is a map value'
        '" null "'  | null              | null                              | 'keyName'                         | true          | true                      | null          | null              | ['keyName': ' null ']                 | null          | null                      | null                                  | 'parsed value is set to null text with blank space between double quotes and attribute is a map value'
        null        | []                | null                              | null                              | true          | false                     | null          | []                | null                                  | null          | []                        | null                                  | 'parsed value list is set to empty list'
        null        | ['']              | null                              | null                              | true          | false                     | null          | ['']              | null                                  | null          | null                      | null                                  | 'parsed value list is set to list with one empty element'
        null        | [' ']             | null                              | null                              | true          | false                     | null          | ['']              | null                                  | null          | null                      | null                                  | 'parsed value list is set to list with one blank element'
        null        | ['" "']           | null                              | null                              | true          | false                     | null          | [' ']             | null                                  | null          | null                      | null                                  | 'parsed value list is set to list with one blank element between double quotes'
        null        | [' " " ']         | null                              | null                              | true          | false                     | null          | [' ']             | null                                  | null          | null                      | null                                  | 'parsed value list is set to list with one blank element between double quotes and blank space outside double quotes'
        null        | [' null', 'null'] | null                              | null                              | true          | false                     | null          | [null, null]      | null                                  | null          | [null, null]              | null                                  | 'parsed value list is set to list null values'
        null        | ['"null " ']      | null                              | null                              | true          | false                     | null          | ['null ']         | null                                  | null          | null                      | null                                  | 'parsed value list element contains text null with blank space between double quotes'
        null        | null              | new HashMap<String, String>()     | null                              | true          | false                     | null          | null              | [:]                                   | null          | null                      | [:]                                   | 'parsed value map is empty'
        null        | null              | ['null':'null']                   | null                              | true          | false                     | null          | null              | getMapOf(null, null)                  | null          | null                      | getMapOf(null, null)                  | 'parsed value map has null key and value'
        null        | null              | ['"null"':'null', 'null':'null']  | null                              | true          | false                     | null          | null              | getMapOf(null, null, "null", null)    | null          | null                      | getMapOf(null, null, "null", null)    | 'parsed value map has multiple null values'
        null        | null              | [' text':' text ']                | null                              | true          | false                     | null          | null              | ['text': 'text']                      | null          | null                      | null                                  | 'parsed value map has text key and text value'
        null        | null              | ['':'']                           | null                              | true          | false                     | null          | null              | ['': '']                              | null          | null                      | null                                  | 'parsed value map has empty text key and value'
        null        | null              | [' ':'  ']                        | null                              | true          | false                     | null          | null              | ['': '']                              | null          | null                      | null                                  | 'parsed value map has blank text key and text value'
        null        | null              | ['" "':' " " ']                   | null                              | true          | false                     | null          | null              | [' ': ' ']                            | null          | null                      | null                                  | 'parsed value map has blank text with double quotes key and value'
        '@ref'      | null              | null                              | null                              | true          | false                     | null          | null              | null                                  | entityMock    | null                      | null                                  | 'parsed value is a reference'
        '"@ref"'    | null              | null                              | null                              | true          | false                     | '@ref'        | null              | null                                  | null          | null                      | null                                  | 'parsed value is a text in double quotes coinciding with the name of a reference'
        ' "@ref" '  | null              | null                              | null                              | true          | false                     | '@ref'        | null              | null                                  | null          | null                      | null                                  | 'parsed value is a text in double quotes with blank space outside coinciding with the name of a reference'
        null        | ['@ref']          | null                              | null                              | true          | false                     | null          | null              | null                                  | null          | [entityMock]              | null                                  | 'parsed value list is a reference'
        null        | ['@ref', '@REF']  | null                              | null                              | true          | false                     | null          | null              | null                                  | null          | [entityMock, entityMock]  | null                                  | 'parsed value list are 2 references'
        '@ref'      | null              | null                              | 'keyName'                         | true          | true                      | null          | null              | null                                  | null          | null                      | ['keyName': entityMock]               | 'parsed value that is a map value for map key is a reference'
        null        | null              | ['key': '@ref']                   | null                              | true          | false                     | null          | null              | null                                  | null          | null                      | ['key': entityMock]                   | 'parsed value map with value that is reference'
    }

    def 'should return correct EntityAttributeValueDto for referenced value when #testDescription'() {
        given:
        EntityAttributeSetter entityAttributeSetterMock = Mock()
        EntityAttributeCharacteristicsDto entityAttributeCharacteristicsMock = Mock()
        Class<?> referencedEntityClass = TestingDummyClass
        EntityAttributeSetter referencedEntityAttribute1Setter = Mock()
        EntityAttributeSetter referencedEntityAttribute2Setter = Mock()
        EntityAttributeCharacteristicsDto referencedEntityAttribute1Characteristics = Mock()
        EntityAttributeCharacteristicsDto referencedEntityAttribute2Characteristics = Mock()

        EntityAttributeDto entityAttribute = new EntityAttributeDto(
                'attributeName',
                false,
                null,
                entityAttributeSetterMock,
                entityAttributeCharacteristicsMock,
                true,
                referencedEntityClass,
                ['attribute1', 'attribute2'],
                [referencedEntityAttribute1Setter, referencedEntityAttribute2Setter],
                [referencedEntityAttribute1Characteristics, referencedEntityAttribute2Characteristics]
        )

        ParsedAttributeValueDto parsedAttributeValue = new ParsedAttributeValueDto(
                parsedValue,
                parsedValueList,
                null
        )
        Map<String, Object> context = ['@ref': entityMock]

        when:
        EntityAttributeValueDto result = entityAttributeService.getEntityAttributeValuesFor([entityAttribute], [parsedAttributeValue], context)[0]

        then:
        genericJpaDaoMock.findReferencedEntityBy(
                referencedEntityClass,
                ['attribute1', 'attribute2'],
                [referencedEntityAttribute1Setter, referencedEntityAttribute2Setter],
                [referencedEntityAttribute1Characteristics, referencedEntityAttribute2Characteristics],
                referencedEntityQueryValues1
        ) >> Optional.of(referencedEntityMock)

        genericJpaDaoMock.findReferencedEntityBy(
                referencedEntityClass,
                ['attribute1', 'attribute2'],
                [referencedEntityAttribute1Setter, referencedEntityAttribute2Setter],
                [referencedEntityAttribute1Characteristics, referencedEntityAttribute2Characteristics],
                referencedEntityQueryValues2
        ) >> Optional.of(referencedEntityMock)

        result.getIsValueSet() == isValueSet
        result.getShouldAppendCollection() == false
        result.getStringValue() == null
        result.getStringValueList() == null
        result.getStringValueMap() == null
        result.getEntityValue() == entityValue
        result.getEntityValueList() == entityValueList
        result.getEntityValueMap() == null

        where:
        parsedValue     | parsedValueList           | referencedEntityQueryValues1  | referencedEntityQueryValues2  | isValueSet    | entityValue           | entityValueList                               | testDescription
        ''              | null                      | null                          | null                          | false         | null                  | null                                          | 'parsed value is empty'
        ' '             | null                      | null                          | null                          | false         | null                  | null                                          | 'parsed value is blank'
        'null'          | null                      | null                          | null                          | true          | null                  | null                                          | 'parsed value is null keyword'
        ' null '        | null                      | null                          | null                          | true          | null                  | null                                          | 'parsed value is null keyword with blank space'
        ' "null" '      | null                      | ['null']                      | null                          | true          | referencedEntityMock  | null                                          | 'parsed value is null keyword with blank space'
        ' val1    '     | null                      | ['val1']                      | null                          | true          | referencedEntityMock  | null                                          | 'parsed value is blank'
        ' "val1"  '     | null                      | ['val1']                      | null                          | true          | referencedEntityMock  | null                                          | 'parsed value is blank'
        'val1 val2'     | null                      | ['val1', 'val2']              | null                          | true          | referencedEntityMock  | null                                          | 'parsed value is blank'
        ' "val1 val2" ' | null                      | ['val1', 'val2']              | null                          | true          | referencedEntityMock  | null                                          | 'parsed value is blank'
        ' "val1 val2" ' | null                      | ['val1', 'val2']              | null                          | true          | referencedEntityMock  | null                                          | 'parsed value is blank'
        ' "val1 @ref" ' | null                      | ['val1', entityMock]          | null                          | true          | referencedEntityMock  | null                                          | 'parsed value is blank'
        ' @ref @ref '   | null                      | [entityMock, entityMock]      | null                          | true          | referencedEntityMock  | null                                          | 'parsed value is blank'
        ' @ref null '   | null                      | [entityMock, 'null']          | null                          | true          | referencedEntityMock  | null                                          | 'parsed value is blank'
        null            | ['@ref abc', 'abc null']  | [entityMock, 'abc']           | ['abc', 'null']               | true          | null                  | [referencedEntityMock, referencedEntityMock]  | 'description'
        null            | ['"@ref a"', '"abc nu"']  | [entityMock, 'a']             | ['abc', 'nu']                 | true          | null                  | [referencedEntityMock, referencedEntityMock]  | 'description'
        null            | ['@ref', 'abc']           | [entityMock]                  | ['abc']                       | true          | null                  | [referencedEntityMock, referencedEntityMock]  | 'description'
        null            | ['null', '"null"']        | ['null']                      | ['null']                      | true          | null                  | [referencedEntityMock, referencedEntityMock]  | 'description'
        null            | ['abc']                   | ['abc']                       | null                          | true          | null                  | [referencedEntityMock]                        | 'description'
        null            | []                        | null                          | null                          | true          | null                  | []                                            | 'description'
    }

    Map<?, ?> 'getMapOf'(Object key, Object value) {
        Map map = new HashMap<Object, Object> ()
        map.put(key, value)
        return map
    }

    Map<?, ?> 'getMapOf'(Object key1, Object value1, Object key2, Object value2) {
        Map map = new HashMap<Object, Object>()
        map.put(key1, value1)
        map.put(key2, value2)
        return map
    }
}
