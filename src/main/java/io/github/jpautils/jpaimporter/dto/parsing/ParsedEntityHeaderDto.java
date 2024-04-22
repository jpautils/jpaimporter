package io.github.jpautils.jpaimporter.dto.parsing;

import java.util.List;

public class ParsedEntityHeaderDto {
    private final List<ParsedEntityAttributeDto> entityAttributes;
    private final boolean shouldKeepEntityReferenceAfterOperation;
    private final Integer entityVariableNameForStoringInContextColumnIndex;

    public ParsedEntityHeaderDto(
            List<ParsedEntityAttributeDto> entityAttributes,
            boolean shouldKeepEntityReferenceAfterOperation,
            Integer entityVariableNameForStoringInContextColumnIndex
    ) {
        this.shouldKeepEntityReferenceAfterOperation = shouldKeepEntityReferenceAfterOperation;
        this.entityAttributes = entityAttributes;
        this.entityVariableNameForStoringInContextColumnIndex = entityVariableNameForStoringInContextColumnIndex;
    }

    public boolean getShouldKeepEntityReferenceAfterOperation() {
        return shouldKeepEntityReferenceAfterOperation;
    }

    public List<ParsedEntityAttributeDto> getEntityAttributes() {
        return entityAttributes;
    }

    public Integer getEntityVariableNameForStoringInContextColumnIndex() {
        return entityVariableNameForStoringInContextColumnIndex;
    }
}
