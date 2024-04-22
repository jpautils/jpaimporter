package io.github.jpautils.jpaimporter.service.impl;

import io.github.jpautils.jpaimporter.configuration.JpaImporterConfiguration;
import io.github.jpautils.jpaimporter.dto.parsing.*;
import io.github.jpautils.jpaimporter.enumeration.ImportOperation;
import io.github.jpautils.jpaimporter.service.CsvParserService;
import io.github.jpautils.jpaimporter.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvParserServiceImpl implements CsvParserService {

    private JpaImporterConfiguration configuration;

    public CsvParserServiceImpl(JpaImporterConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<ParsedEntityImportBatchDto> parseCsvLines(List<String> csvLines) {
        List<ParsedEntityImportBatchDto> parsedBatchImports = new ArrayList<>();
        List<String> csvBlock = new ArrayList<>();

        for (String currentLine : csvLines) {
            if (currentLine.trim().startsWith("#")) {
                continue;
            }

            if (currentLine.isBlank()) {
                List<String> nonBlankLines = csvBlock.stream().filter(Util::stringIsNotBlank).collect(Collectors.toList());

                if (nonBlankLines.size() > 0) {
                    ParsedEntityImportBatchDto parsedBatchImport = parseCsvBlock(nonBlankLines);
                    parsedBatchImports.add(parsedBatchImport);
                }

                csvBlock = new ArrayList<>();
            } else {
                csvBlock.add(currentLine);
            }
        }

        List<String> nonBlankLines = csvBlock.stream().filter(Util::stringIsNotBlank).collect(Collectors.toList());
        if (nonBlankLines.size() > 0) {
            ParsedEntityImportBatchDto parsedBatchImport = parseCsvBlock(nonBlankLines);
            parsedBatchImports.add(parsedBatchImport);
        }

        return parsedBatchImports;
    }

    private ParsedEntityImportBatchDto parseCsvBlock(List<String> csvLines) {
        List<String> nonBlankLines = csvLines
                .stream()
                .filter(Util::stringIsNotBlank)
                .collect(Collectors.toList());

        String header = nonBlankLines.get(0);
        List<String> headerColumns = Util.splitByCharacter(header, ';');

        String importOperationText = Util.splitByCharacter(headerColumns.get(0).trim(), ' ').get(0);
        ImportOperation importOperation = ImportOperation.valueOf(importOperationText.toUpperCase());

        String entityName =  Util.splitByCharacter(headerColumns.get(0).trim(), ' ')
                .stream()
                .filter(Util::stringIsNotBlank)
                .collect(Collectors.toList())
                .get(1);

        ParsedEntityHeaderDto entityAttributes = parseCsvHeader(headerColumns);

        List<ParsedAttributeValuesRowDto> parsedValuesRows = new ArrayList<>();

        for (int lineNumber = 1; lineNumber < nonBlankLines.size(); lineNumber++) {
            String csvLine = nonBlankLines.get(lineNumber);

            ParsedAttributeValuesRowDto parsedEntityValues = parseCsvLine(entityAttributes, csvLine);

            parsedValuesRows.add(parsedEntityValues);
        }

        ParsedEntityImportBatchDto entityBatchImport = new ParsedEntityImportBatchDto(
                importOperation,
                entityName,
                entityAttributes,
                parsedValuesRows
        );

        return entityBatchImport;
    }

    private ParsedEntityHeaderDto parseCsvHeader(List<String> headerColumns) {

        int referencingColumnsCount = headerColumns.stream().map(String::trim).filter(s -> s.equalsIgnoreCase("@reference")).collect(Collectors.toList()).size();
        boolean shouldPutEntityReferenceInContext = referencingColumnsCount == 1;

        if (referencingColumnsCount > 1) {
            throw new RuntimeException("Import  has [" + referencingColumnsCount + "] @reference fields. There should be maximum 1. Header: [" + headerColumns + "]");
        }

        Integer referenceColumnNumber = null;

        List<ParsedEntityAttributeDto> entityAttributes = new ArrayList<>();

        for (int i = 1; i < headerColumns.size(); i++) {
            final String attributeName;
            final boolean isAttributeUnique;
            final String mapKeyName;
            final boolean isAttributeReferencingEntity;
            final List<String> referencedEntityUniqueFields;

            String columnField = headerColumns.get(i).trim();

            if (columnField.trim().equalsIgnoreCase("@reference")) {
                referenceColumnNumber = i;
                continue;
            }

            if (columnField.toLowerCase().contains("[unique]") && !columnField.contains("[unique]")) {
                throw new RuntimeException("Column: [" + columnField + "] has invalid unique identifier. Unique keyword should be lowercase.");
            }

            if (columnField.contains("[unique]")) {
                columnField = columnField.replace("[unique]", "");
                isAttributeUnique = true;
            } else {
                isAttributeUnique = false;
            }

            if (columnField.contains("[") && columnField.contains("]")) {
                int openingSquareBracketIndex = columnField.indexOf("[");
                int closingSquareBracketIndex = columnField.indexOf("]");
                String squareBracketText = columnField.substring(openingSquareBracketIndex, closingSquareBracketIndex + 1);
                String textBetweenSquareBrackets = squareBracketText.substring(1, squareBracketText.length() - 1);
                columnField = columnField.replace(squareBracketText, "");
                if (Util.stringIsNotBlank(textBetweenSquareBrackets)) {
                    mapKeyName = textBetweenSquareBrackets.trim();
                } else {
                    mapKeyName = null;
                }
            } else {
                mapKeyName = null;
            }

            if (columnField.contains("(") && columnField.contains(")")) {
                int openingParanthesisIndex = columnField.indexOf("(");
                int closingParanthesisIndex = columnField.lastIndexOf(")");
                String paranthesisText = columnField.substring(openingParanthesisIndex, closingParanthesisIndex + 1);
                String textBetweenParanthesis = paranthesisText.substring(1, paranthesisText.length() - 1);
                attributeName = columnField.replace(paranthesisText, "");
                isAttributeReferencingEntity = true;
                referencedEntityUniqueFields = Util.splitByCharacter(textBetweenParanthesis, ' ')
                        .stream()
                        .map(String::trim)
                        .filter(Util::stringIsNotBlank)
                        .collect(Collectors.toList());
            } else {
                attributeName = columnField;
                isAttributeReferencingEntity = false;
                referencedEntityUniqueFields = null;
            }

            ParsedEntityAttributeDto entityField = new ParsedEntityAttributeDto(
                    attributeName,
                    isAttributeUnique,
                    mapKeyName,
                    isAttributeReferencingEntity,
                    referencedEntityUniqueFields
            );

            entityAttributes.add(entityField);
        }

        ParsedEntityHeaderDto parsedEntityAttributes = new ParsedEntityHeaderDto(
                entityAttributes,
                shouldPutEntityReferenceInContext,
                referenceColumnNumber
        );

        return parsedEntityAttributes;
    }

    private ParsedAttributeValuesRowDto parseCsvLine(ParsedEntityHeaderDto entityAttributes, String line) {
        String escapedLine = escapeString(line);

        List<String> lineColumns = Util.splitByCharacter(escapedLine, ';');

        final String entityVariableNameForStoringInContext;
        if (entityAttributes.getShouldKeepEntityReferenceAfterOperation()) {
            String escapedEntityVariableNameForStoringInContext = lineColumns.get(entityAttributes.getEntityVariableNameForStoringInContextColumnIndex());
            entityVariableNameForStoringInContext = unescapeSpecialCharacters(escapedEntityVariableNameForStoringInContext.trim());
            lineColumns.remove(entityAttributes.getEntityVariableNameForStoringInContextColumnIndex().intValue());
        } else {
            entityVariableNameForStoringInContext = null;
        }

        if ((entityAttributes.getEntityAttributes().size() + 1) != lineColumns.size()) {
            throw new RuntimeException("Header columns: [" + (entityAttributes.getEntityAttributes().size()+1) + "]. Line columns: [" + lineColumns.size() + "]. " +
                    "The numbers should match. Line value: [" + line + "]");
        }

        List<ParsedAttributeValueDto> entityValues = new ArrayList<>();

        for (int i = 1; i < lineColumns.size(); i++) {
            final String fieldValue;
            final List<String> fieldValueList;
            final Map<String, String> attributeValueMap;

            String columnValue = lineColumns.get(i).trim();

            if (columnValue.startsWith("[") && columnValue.endsWith("]")) {
                String columnValueWithoutBrackets = columnValue.substring(1, columnValue.length() - 1);

                List<String> splitColumnValues = Util.splitByCharacter(columnValueWithoutBrackets, ',')
                        .stream()
                        .map(String::trim)
                        .collect(Collectors.toList());

                if (columnValueWithoutBrackets.contains(":")) {
                    if (columnValueWithoutBrackets.trim().equals(":")) {
                        attributeValueMap = new HashMap<>();
                    } else {
                        Map<String, String> parsedValuesMap = new HashMap<>();
                        for (String splitColumnValue: splitColumnValues) {
                            List<String> keyValue = Util.splitByCharacter(splitColumnValue, ':');

                            if (keyValue.size() != 2) {
                                throw new RuntimeException("Error when trying to parse column value: [" + columnValue + "]. Element: [" + splitColumnValue + "] should split in 2 elements, but was split in: [" + keyValue + "]");
                            }

                            parsedValuesMap.put(unescapeSpecialCharacters(keyValue.get(0).trim()), unescapeSpecialCharacters(keyValue.get(1).trim()));
                        }

                        attributeValueMap = parsedValuesMap;
                    }

                    fieldValueList = null;
                    fieldValue = null;
                } else {
                    List<String> unescapedSplitColumnValuesList = splitColumnValues.stream().map(this::unescapeSpecialCharacters).collect(Collectors.toList());
                    if (splitColumnValues.size() == 1 && splitColumnValues.get(0).isEmpty()) {
                        fieldValueList = new ArrayList<>();
                    } else {
                        fieldValueList = unescapedSplitColumnValuesList;
                    }
                    attributeValueMap = null;
                    fieldValue = null;
                }
            } else {
                fieldValue = unescapeSpecialCharacters(columnValue);
                fieldValueList = null;
                attributeValueMap = null;
            }

            ParsedAttributeValueDto importableEntityParsedValueDto = new ParsedAttributeValueDto(
                    fieldValue,
                    fieldValueList,
                    attributeValueMap
            );

            entityValues.add(importableEntityParsedValueDto);
        }

        return new ParsedAttributeValuesRowDto(
                entityValues,
                entityVariableNameForStoringInContext
        );
    }

    private String escapeString(String text) {
        String escapedTextPhase1 = escapeSpecialCharacters(text);
        String escapedTextPhase2 = escapeCommaAndColonAndSemicolonBetweenDoubleQuotes(escapedTextPhase1);

        return escapedTextPhase2;
    }

    private String escapeSpecialCharacters(String text) {
        if (text.length() < 2) {
            return text;
        }

        StringBuilder escapedText = new StringBuilder();

        for (int i = 0 ; i < text.length() - 1 ; i++) {
            char currentChar = text.charAt(i);
            char nextChar = text.charAt(i+1);

            if (currentChar == '\\' && (nextChar == '\\' || nextChar == '\"' || nextChar == 'n')) {
                i++;
                switch (nextChar) {
                    case '\\': escapedText.append(configuration.getBackslashPlaceholder()); break;
                    case '"': escapedText.append(configuration.getDoubleQuotePlaceholder()); break;
                    case 'n': escapedText.append(configuration.getNewlinePlaceholder()); break;
                    default: throw new RuntimeException("Could not parse special character: [" + currentChar + nextChar + "] in text :[" + text + "].");
                }
            } else {
                escapedText.append(currentChar);

                if (i == text.length() - 2) {
                    escapedText.append(nextChar);
                }
            }
        }

        return escapedText.toString();
    }

    private String escapeCommaAndColonAndSemicolonBetweenDoubleQuotes(String text) {
        StringBuilder encodedText = new StringBuilder();
        int encounteredDoubleQuoteCount = 0;

        for (int i = 0 ; i < text.length() ; i++) {
            char currentChar = text.charAt(i);

            if (currentChar == '\"') {
                encounteredDoubleQuoteCount++;
            }

            if ((currentChar == ',' || currentChar == ';' || currentChar == ':') && Util.isOdd(encounteredDoubleQuoteCount)) {
                switch (currentChar) {
                    case ';': encodedText.append(configuration.getSemicolonBetweenDoubleQuotesPlaceholder()); break;
                    case ':': encodedText.append(configuration.getColonBetweenDoubleQuotesPlaceholder()); break;
                    case ',': encodedText.append(configuration.getCommaBetweenDoubleQuotesPlaceholder()); break;
                    default: throw new RuntimeException("Could not parse char: [" + currentChar + "] in text: [" + text + "].");
                }
            } else {
                encodedText.append(currentChar);
            }
        }

        return encodedText.toString();
    }

    private String unescapeSpecialCharacters(String text) {
        return text
                .replace(configuration.getNewlinePlaceholder(), "\n")
                .replace(configuration.getSemicolonBetweenDoubleQuotesPlaceholder(), ";")
                .replace(configuration.getColonBetweenDoubleQuotesPlaceholder(), ":")
                .replace(configuration.getCommaBetweenDoubleQuotesPlaceholder(), ",")
                .replace(configuration.getBackslashPlaceholder(), "\\")
                .replace(configuration.getDoubleQuotePlaceholder(), "\"");
    }
}
