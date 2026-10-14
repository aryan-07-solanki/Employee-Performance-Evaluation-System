package com.epes.exception;

/** Wraps SQLException so upper layers do not depend on JDBC classes. */
public class DatabaseException extends Exception {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
