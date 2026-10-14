package com.epes.exception;

/** Thrown when a database record (user, goal, ...) does not exist. */
public class RecordNotFoundException extends Exception {
    public RecordNotFoundException(String message) {
        super(message);
    }
}
