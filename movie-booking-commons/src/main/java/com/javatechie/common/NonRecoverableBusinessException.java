package com.javatechie.common;

public class NonRecoverableBusinessException extends RuntimeException {

    public NonRecoverableBusinessException(String message) {
        super(message);
    }
}
