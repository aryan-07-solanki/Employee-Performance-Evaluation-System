package com.epes.exception;

/** Thrown when the email or password is wrong during login. */
public class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
