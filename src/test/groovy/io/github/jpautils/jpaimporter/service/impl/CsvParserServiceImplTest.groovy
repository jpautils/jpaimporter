package io.github.jpautils.jpaimporter.service.impl

import io.github.jpautils.jpaimporter.configuration.impl.JpaImporterConfigurationImpl
import io.github.jpautils.jpaimporter.dto.parsing.ParsedAttributeValueDto
import io.github.jpautils.jpaimporter.dto.parsing.ParsedAttributeValuesRowDto
import io.github.jpautils.jpaimporter.dto.parsing.ParsedEntityAttributeDto
import io.github.jpautils.jpaimporter.dto.parsing.ParsedEntityHeaderDto
import io.github.jpautils.jpaimporter.dto.parsing.ParsedEntityImportBatchDto
import io.github.jpautils.jpaimporter.enumeration.ImportOperation
import spock.lang.Specification

import static org.junit.jupiter.api.Assertions.assertThrows

class CsvParserServiceImplTest extends Specification {
    CsvParserServiceImpl csvParserService = new CsvParserServiceImpl(new JpaImporterConfigurationImpl());

    def "Should parse correctly import operation and entity name"() {
        String sampleCsvLine1 = "UPSERT Entity ; code "
        String sampleCsvLine2 = "              ; code "
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2)

        ParsedEntityImportBatchDto result = csvParserService.parseCsvLines(csvLines)[0]

        expect:
        ImportOperation.UPSERT == result.importOperation
        "Entity" == result.entityName
    }


    def "Should parse correctly import operations that are not uppercase"() {
        String sampleCsvLine1 = "upSert Entity ; code "
        String sampleCsvLine2 = "              ; code "
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2)

        ParsedEntityImportBatchDto result = csvParserService.parseCsvLines(csvLines)[0]

        expect:
        ImportOperation.UPSERT == result.importOperation
    }

    def "Should throw error if the column unique tag is not lowercase"() {
        String sampleCsvLine1 = "UPSERT Entity2 ; code[UNIque]"
        String sampleCsvLine2 = "               ; sampleCode  "
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2)

        Exception exception = assertThrows(RuntimeException.class, () -> csvParserService.parseCsvLines(csvLines))

        expect:
        "Column: [code[UNIque]] has invalid unique identifier. Unique keyword should be lowercase." == exception.message
    }

    def "Should parse correctly column that references another entity"() {
        String sampleCsvLine1 = "UPSERT Entity ; field(referencedEntityField) "
        String sampleCsvLine2 = "              ; referencedEntityUniqueKey    "
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2)

        ParsedEntityImportBatchDto result = csvParserService.parseCsvLines(csvLines)[0]
        ParsedEntityAttributeDto parsedAttribute = result.entityHeader.entityAttributes[0]
        ParsedAttributeValueDto parsedValue = result.entityValuesRows[0].attributeValues[0]

        expect:
        "field" == parsedAttribute.attributeName
        parsedAttribute.isAttributeReferencingEntity
        !parsedAttribute.isUnique
        "referencedEntityField" == parsedAttribute.referencedEntityUniqueAttributes[0]
        "referencedEntityUniqueKey" == parsedValue.attributeValue
        null == parsedValue.attributeValuesList
        null == parsedValue.attributeValuesMap
    }

    def "Should parse correctly column that references another entity by multiple fields"() {
        String sampleCsvLine1 = "UPSERT Entity ; field(  referencedEntityField1  referencedEntityField2 referencedEntityField3)    "
        String sampleCsvLine2 = "              ; referencedEntityField1Value   referencedEntityField2Value referencedEntityField3Value  "
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2)

        ParsedEntityImportBatchDto result = csvParserService.parseCsvLines(csvLines)[0]
        ParsedEntityAttributeDto parsedAttribute = result.entityHeader.entityAttributes[0]
        ParsedAttributeValueDto parsedValue = result.entityValuesRows[0].attributeValues[0]

        expect:
        "field" == parsedAttribute.attributeName
        parsedAttribute.isAttributeReferencingEntity
        !parsedAttribute.isUnique
        ["referencedEntityField1", "referencedEntityField2", "referencedEntityField3"] == parsedAttribute.referencedEntityUniqueAttributes
        "referencedEntityField1Value   referencedEntityField2Value referencedEntityField3Value" == parsedValue.attributeValue
    }

    def "Should parse correctly column that has unique tag and references another entity"() {
        String sampleCsvLine1 = "UPSERT Entity ; field(referencedEntityField)[unique] "
        String sampleCsvLine2 = "              ; referencedEntityUniqueKey    "
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2)

        ParsedEntityImportBatchDto result = csvParserService.parseCsvLines(csvLines)[0]
        ParsedEntityAttributeDto parsedAttribute = result.entityHeader.entityAttributes[0]
        ParsedAttributeValueDto parsedValue = result.entityValuesRows[0].attributeValues[0]

        expect:
        "field" == parsedAttribute.attributeName
        parsedAttribute.isAttributeReferencingEntity
        parsedAttribute.isUnique
        ["referencedEntityField"] == parsedAttribute.referencedEntityUniqueAttributes
        "referencedEntityUniqueKey" == parsedValue.attributeValue
    }

    def "Should parse correctly map key in csv header"() {
        String sampleCsvLine1 = "UPSERT Entity ; field[mapKey](referencedEntityField)[unique] "
        String sampleCsvLine2 = "              ; referencedEntityUniqueKey    "
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2)

        ParsedEntityImportBatchDto result = csvParserService.parseCsvLines(csvLines)[0]
        ParsedEntityAttributeDto parsedAttribute = result.entityHeader.entityAttributes[0]
        ParsedAttributeValueDto parsedValue = result.entityValuesRows[0].attributeValues[0]

        expect:
        "field" == parsedAttribute.attributeName
        "mapKey" == parsedAttribute.attributeIsAMapValueForMapKeyName
        parsedAttribute.isAttributeReferencingEntity
        parsedAttribute.isUnique
        ["referencedEntityField"] == parsedAttribute.referencedEntityUniqueAttributes
        "referencedEntityUniqueKey" == parsedValue.attributeValue
        null == parsedValue.attributeValuesList
        null == parsedValue.attributeValuesMap
    }

    def "Should parse correctly map key in csv header and trim blank space"() {
        String sampleCsvLine1 = "UPSERT Entity ; field(referencedEntityField)[unique][ mapKey ] "
        String sampleCsvLine2 = "              ; referencedEntityUniqueKey    "
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2)

        ParsedEntityImportBatchDto result = csvParserService.parseCsvLines(csvLines)[0]
        ParsedEntityAttributeDto parsedAttribute = result.entityHeader.entityAttributes[0]
        ParsedAttributeValueDto parsedValue = result.entityValuesRows[0].attributeValues[0]

        expect:
        "field" == parsedAttribute.attributeName
        "mapKey" == parsedAttribute.attributeIsAMapValueForMapKeyName
        parsedAttribute.isAttributeReferencingEntity
        parsedAttribute.isUnique
        ["referencedEntityField"] == parsedAttribute.referencedEntityUniqueAttributes
        "referencedEntityUniqueKey" == parsedValue.attributeValue
        null == parsedValue.attributeValuesList
        null == parsedValue.attributeValuesMap
    }

    def "Should parse correctly map key in csv header and set it to null if there is blank value"() {
        String sampleCsvLine1 = "UPSERT Entity ; field(referencedEntityField)[unique][  ] "
        String sampleCsvLine2 = "              ; referencedEntityUniqueKey    "
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2)

        ParsedEntityImportBatchDto result = csvParserService.parseCsvLines(csvLines)[0]
        ParsedEntityAttributeDto parsedAttribute = result.entityHeader.entityAttributes[0]
        ParsedAttributeValueDto parsedValue = result.entityValuesRows[0].attributeValues[0]

        expect:
        "field" == parsedAttribute.attributeName
        null == parsedAttribute.attributeIsAMapValueForMapKeyName
        parsedAttribute.isAttributeReferencingEntity
        parsedAttribute.isUnique
        ["referencedEntityField"] == parsedAttribute.referencedEntityUniqueAttributes
        "referencedEntityUniqueKey" == parsedValue.attributeValue
    }

    def "Should parse correctly map key in csv header and set it to null if there is no value"() {
        String sampleCsvLine1 = "UPSERT Entity ; field(referencedEntityField)[][unique] "
        String sampleCsvLine2 = "              ; referencedEntityUniqueKey    "
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2)

        ParsedEntityImportBatchDto result = csvParserService.parseCsvLines(csvLines)[0]
        ParsedEntityAttributeDto parsedAttribute = result.entityHeader.entityAttributes[0]
        ParsedAttributeValueDto parsedValue = result.entityValuesRows[0].attributeValues[0]

        expect:
        "field" == parsedAttribute.attributeName
        null == parsedAttribute.attributeIsAMapValueForMapKeyName
        parsedAttribute.isAttributeReferencingEntity
        parsedAttribute.isUnique
        ["referencedEntityField"] == parsedAttribute.referencedEntityUniqueAttributes
        "referencedEntityUniqueKey" == parsedValue.attributeValue
    }

    def "Should parse correctly @reference special column"() {
        String sampleCsvLine1 = "UPSERT Entity ; field(referencedEntityField)[][unique] ; @reference"
        String sampleCsvLine2 = "              ; referencedEntityUniqueKey              ;   1 23  "
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2)

        ParsedEntityImportBatchDto result = csvParserService.parseCsvLines(csvLines)[0]
        ParsedEntityHeaderDto parsedHeader = result.entityHeader
        ParsedAttributeValuesRowDto parsedValueRow = result.entityValuesRows[0]

        expect:
        parsedHeader.shouldKeepEntityReferenceAfterOperation
        parsedHeader.entityVariableNameForStoringInContextColumnIndex == 2
        parsedHeader.entityAttributes.size() == 1
        parsedHeader.entityAttributes[0].attributeName == "field"

        parsedValueRow.entityVariableNameForStoringInContext == "1 23"
        parsedValueRow.attributeValues.size() == 1
        parsedValueRow.attributeValues[0].attributeValue == "referencedEntityUniqueKey"
    }

    def "Should parse correctly @reference special column when there is a single column"() {
        String sampleCsvLine1 = "UPSERT Entity ; @reference"
        String sampleCsvLine2 = "              ;    1 23  "
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2)

        ParsedEntityImportBatchDto result = csvParserService.parseCsvLines(csvLines)[0]
        ParsedEntityHeaderDto parsedHeader = result.entityHeader
        ParsedAttributeValuesRowDto parsedValueRow = result.entityValuesRows[0]

        expect:
        parsedHeader.shouldKeepEntityReferenceAfterOperation
        parsedHeader.entityVariableNameForStoringInContextColumnIndex == 1
        parsedHeader.entityAttributes.size() == 0

        parsedValueRow.entityVariableNameForStoringInContext == "1 23"
        parsedValueRow.attributeValues.size() == 0
    }

    def "Should parse correctly value"() {
        String sampleCsvLine1 = "UPSERT Entity ; field "
        String sampleCsvLine2 = "              ; " + csvValue
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2)

        ParsedEntityImportBatchDto result = csvParserService.parseCsvLines(csvLines)[0]
        String parsedValue = result.entityValuesRows[0].attributeValues[0].attributeValue
        List<String> parsedValueList = result.entityValuesRows[0].attributeValues[0].attributeValuesList
        Map<String, String> parsedValueMap = result.entityValuesRows[0].attributeValues[0].attributeValuesMap

        expect:
        expectedParsedValue == parsedValue
        expectedParsedValueList == parsedValueList
        expectedParsedValueMap == parsedValueMap

        where:
        csvValue                        | expectedParsedValue   | expectedParsedValueList   | expectedParsedValueMap
        ""                              | ""                    | null                      | null
        "      "                        | ""                    | null                      | null
        "null"                          | "null"                | null                      | null
        "  abc   "                      | "abc"                 | null                      | null
        '  " abc "     '                | '" abc "'             | null                      | null
        "  \"\\n\"  "                   | "\"\n\""              | null                      | null
        """ "\\"  """                   | """"\""""             | null                      | null
        """ "\"" """                    | "\"\"\""              | null                      | null
        "  \"[]\"   "                   | "\"[]\""              | null                      | null
        " \"a;c,d\"     "               | "\"a;c,d\""           | null                      | null
        " 1.34  "                       | "1.34"                | null                      | null
        " false"                        | "false"               | null                      | null
        "3,14"                          | "3,14"                | null                      | null
        " [    ] "                      | null                  | []                        | null
        " [  [ ]  ] "                   | null                  | ["[ ]"]                   | null
        " [,,]"                         | null                  | ["","",""]                | null
        " [   \"\" ]"                   | null                  | ["\"\""]                  | null
        "[abc]"                         | null                  | ["abc"]                   | null
        "[ abc , de f ]"                | null                  | ["abc", "de f"]           | null
        " [ a, \" \", ] "               | null                  | ["a", "\" \"", ""]        | null
        " [ \";\\\", \\\\, \\n,\",  ] " | null                  | ["\";\", \\, \n,\"", ""]  | null
        "[:]"                           | null                  | null                      | [:]
        "[  :  ]"                       | null                  | null                      | [:]
        "[  :  ,  :]"                   | null                  | null                      | ["":""]
        "[:abc]"                        | null                  | null                      | ["":"abc"]
        "[abc:]"                        | null                  | null                      | ["abc":""]
        "[a:b]"                         | null                  | null                      | ["a": "b"]
        "[\"a\":b]"                     | null                  | null                      | ["\"a\"": "b"]
        "[a:\"b[a:b,ag]\"]"             | null                  | null                      | ["a": "\"b[a:b,ag]\""]
        "[a:b,a:c]"                     | null                  | null                      | ["a": "c"]
    }

    def "Should correctly parse multiple lines"() {
        String sampleCsvLine1 = "insert Entity ; @reference ; field1[unique](code)          ; field2         "
        String sampleCsvLine2 = "              ; ref1       ; \" some ; , [] text \\\\ \"   ; [\",,\",,[]]   "
        String sampleCsvLine3 = "              ;            ; text aaa                      ;                "
        List<String> csvLines = Arrays.asList(sampleCsvLine1, sampleCsvLine2, sampleCsvLine3)

        ParsedEntityImportBatchDto result = csvParserService.parseCsvLines(csvLines)[0]

        expect:
        result.importOperation == ImportOperation.INSERT
        result.entityName == "Entity"
        result.entityHeader.shouldKeepEntityReferenceAfterOperation
        result.entityHeader.entityAttributes[0].isUnique
        result.entityHeader.entityAttributes[0].attributeName == "field1"
        result.entityHeader.entityAttributes[0].isAttributeReferencingEntity
        result.entityHeader.entityAttributes[0].referencedEntityUniqueAttributes == ["code"]
        result.entityHeader.entityAttributes[1].isUnique == false
        result.entityHeader.entityAttributes[1].attributeName == "field2"
        result.entityHeader.entityAttributes[1].isAttributeReferencingEntity == false
        result.entityHeader.entityAttributes[1].referencedEntityUniqueAttributes == null

        result.entityValuesRows[0].entityVariableNameForStoringInContext == "ref1"
        result.entityValuesRows[0].attributeValues[0].attributeValue == "\" some ; , [] text \\ \""
        result.entityValuesRows[0].attributeValues[0].attributeValuesList == null
        result.entityValuesRows[0].attributeValues[0].attributeValuesMap == null
        result.entityValuesRows[0].attributeValues[1].attributeValue == null
        result.entityValuesRows[0].attributeValues[1].attributeValuesList == ["\",,\"", "", "[]"]
        result.entityValuesRows[0].attributeValues[1].attributeValuesMap == null

        result.entityValuesRows[1].entityVariableNameForStoringInContext == ""
        result.entityValuesRows[1].attributeValues[0].attributeValue == "text aaa"
        result.entityValuesRows[1].attributeValues[0].attributeValuesList == null
        result.entityValuesRows[1].attributeValues[0].attributeValuesMap == null
        result.entityValuesRows[1].attributeValues[1].attributeValue == ""
        result.entityValuesRows[1].attributeValues[1].attributeValuesList == null
        result.entityValuesRows[1].attributeValues[1].attributeValuesMap == null
    }
}
