package com.markdowncollab.exception;

// Document exceptions
public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(String message) {
        super(message);
    }
}

// User exceptions
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}

// Version exceptions
public class VersionNotFoundException extends RuntimeException {
    public VersionNotFoundException(String message) {
        super(message);
    }
}

// Export exceptions
public class ExportException extends Exception {
    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class UnsupportedExportFormatException extends RuntimeException {
    public UnsupportedExportFormatException(String message) {
        super(message);
    }
}