package io.github.jpautils.jpaimporter.service;

import io.github.jpautils.jpaimporter.dto.parsing.ParsedEntityImportBatchDto;

import java.util.Map;

public interface EntityImportService {
    void importEntities(ParsedEntityImportBatchDto entityImportBatch, Map<String, Object> context);
}
