package com.anshmidt.pricemonitor.data;

// it's unchecked because of Stream API restrictions
public class EmptyDataException extends RuntimeException {
    public EmptyDataException() {
    }

    public EmptyDataException(String message) {
        super(message);
    }
}
