package io.github.jpautils.jpaimporter.dto.parsing;

import io.github.jpautils.jpaimporter.enumeration.ImportOperation;

import java.util.List;

public class ParsedEntityImportBatchDto {
    private final ImportOperation importOperation;
    private final String entityName;
    private final ParsedEntityHeaderDto entityHeader;
    private final List<ParsedAttributeValuesRowDto> entityValuesRows;

    public ParsedEntityImportBatchDto(
            ImportOperation importOperation,
            String entityName,
            ParsedEntityHeaderDto entityHeader,
            List<ParsedAttributeValuesRowDto> entityValuesRows
    ) {
        this.importOperation = importOperation;
        this.entityName = entityName;
        this.entityHeader = entityHeader;
        this.entityValuesRows = entityValuesRows;
    }

    public ImportOperation getImportOperation() {
        return importOperation;
    }

    public String getEntityName() {
        return entityName;
    }

    public ParsedEntityHeaderDto getEntityHeader() {
        return entityHeader;
    }

    public List<ParsedAttributeValuesRowDto> getEntityValuesRows() {
        return entityValuesRows;
    }
}
