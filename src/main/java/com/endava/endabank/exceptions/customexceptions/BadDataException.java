package com.endava.endabank.exceptions.customexceptions;

public class BadDataException extends RuntimeException {
    public BadDataException(String msg) {
        super(msg);
    }
}
