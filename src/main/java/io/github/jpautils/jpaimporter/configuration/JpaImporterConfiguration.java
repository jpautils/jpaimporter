package io.github.jpautils.jpaimporter.configuration;

public interface JpaImporterConfiguration {
    String getBackslashPlaceholder();
    String getDoubleQuotePlaceholder();
    String getNewlinePlaceholder();
    String getSemicolonBetweenDoubleQuotesPlaceholder();
    String getCommaBetweenDoubleQuotesPlaceholder();
    String getColonBetweenDoubleQuotesPlaceholder();
}
