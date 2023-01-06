package com.mrshiehx.file.manager.exceptions;

public class NoRootPermissionException extends RuntimeException {
    public NoRootPermissionException() {
        super();
    }

    public NoRootPermissionException(String message) {
        super(message);
    }

    public NoRootPermissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoRootPermissionException(Throwable cause) {
        super(cause);
    }
}
