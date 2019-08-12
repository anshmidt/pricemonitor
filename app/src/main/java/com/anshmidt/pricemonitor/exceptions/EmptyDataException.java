package com.anshmidt.pricemonitor.exceptions;

public class EmptyDataException extends Exception {
    public EmptyDataException() {
    }

    public EmptyDataException(String message) {
        super(message);
    }
}
