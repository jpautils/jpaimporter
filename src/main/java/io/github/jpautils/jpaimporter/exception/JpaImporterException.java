package io.github.jpautils.jpaimporter.exception;

public class JpaImporterException extends RuntimeException{
    public JpaImporterException(String message, Exception exception) {
        super(message, exception);
    }
}
