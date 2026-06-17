package com.centilontech.gateway.client;

public class AccountServiceUnavailableException extends RuntimeException {
    public AccountServiceUnavailableException(Throwable cause) {
        super("Account Service is currently unavailable. Event was not applied.", cause);
    }
}
