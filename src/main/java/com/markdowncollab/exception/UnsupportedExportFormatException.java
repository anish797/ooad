package com.markdowncollab.exception;

public class UnsupportedExportFormatException extends RuntimeException {
    public UnsupportedExportFormatException(String message) {
        super(message);
    }
}