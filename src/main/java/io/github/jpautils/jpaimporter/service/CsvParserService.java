package io.github.jpautils.jpaimporter.service;

import io.github.jpautils.jpaimporter.dto.parsing.ParsedEntityImportBatchDto;

import java.util.List;

public interface CsvParserService {
    List<ParsedEntityImportBatchDto> parseCsvLines(List<String> csvLines);
}
