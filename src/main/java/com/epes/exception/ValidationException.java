package com.epes.exception;

/** Thrown when user input is empty or in the wrong format. */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
