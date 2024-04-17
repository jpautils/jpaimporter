package io.github.jpautils.jpaimporter.dto.parsing;

import java.util.List;

public class ParsedEntityHeaderDto {
    private final List<ParsedEntityAttributeDto> entityAttributes;
    private final boolean shouldKeepEntityReferenceAfterOperation;
    private final Integer entityReferenceNameColumnIndex;

    public ParsedEntityHeaderDto(
            List<ParsedEntityAttributeDto> entityAttributes,
            boolean shouldKeepEntityReferenceAfterOperation,
            Integer entityReferenceNameColumnIndex
    ) {
        this.shouldKeepEntityReferenceAfterOperation = shouldKeepEntityReferenceAfterOperation;
        this.entityAttributes = entityAttributes;
        this.entityReferenceNameColumnIndex = entityReferenceNameColumnIndex;
    }

    public boolean getShouldKeepEntityReferenceAfterOperation() {
        return shouldKeepEntityReferenceAfterOperation;
    }

    public List<ParsedEntityAttributeDto> getEntityAttributes() {
        return entityAttributes;
    }

    public Integer getEntityReferenceNameColumnIndex() {
        return entityReferenceNameColumnIndex;
    }
}
