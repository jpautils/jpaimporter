package io.github.jpautils.jpaimporter.configuration.impl;

import io.github.jpautils.jpaimporter.configuration.JpaImporterConfiguration;

public class JpaImporterConfigurationImpl implements JpaImporterConfiguration {
    private static final String BACKSLASH_PLACEHOLDER = "{BackslashPlaceHolder}";
    private static final String DOUBLE_QUOTE_PLACEHOLDER = "{DoubleQuotePlaceHolder}";
    private static final String NEWLINE_PLACEHOLDER = "{NewlinePlaceholder}";
    private static final String SEMICOLON_BETWEEN_DOUBLE_QUOTES_PLACEHOLDER = "{SemicolonBetweenDoubleQuotesPlaceholder}";
    private static final String COMMA_BETWEEN_DOUBLE_QUOTES_PLACEHOLDER = "{CommaBetweenDoubleQuotesPlaceholder}";
    private static final String COLON_BETWEEN_DOUBLE_QUOTES_PLACEHOLDER = "{ColonBetweenDoubleQuotesPlaceholder}";

    @Override
    public String getBackslashPlaceholder() {
        return BACKSLASH_PLACEHOLDER;
    }

    @Override
    public String getDoubleQuotePlaceholder() {
        return DOUBLE_QUOTE_PLACEHOLDER;
    }

    @Override
    public String getNewlinePlaceholder() {
        return NEWLINE_PLACEHOLDER;
    }

    @Override
    public String getSemicolonBetweenDoubleQuotesPlaceholder() {
        return SEMICOLON_BETWEEN_DOUBLE_QUOTES_PLACEHOLDER;
    }

    @Override
    public String getCommaBetweenDoubleQuotesPlaceholder() {
        return COMMA_BETWEEN_DOUBLE_QUOTES_PLACEHOLDER;
    }

    @Override
    public String getColonBetweenDoubleQuotesPlaceholder() {
        return COLON_BETWEEN_DOUBLE_QUOTES_PLACEHOLDER;
    }
}
