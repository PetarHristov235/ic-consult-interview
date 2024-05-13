package com.icconsult.interview.usermanagement.exception;

public class CustomerNotFoundException extends RuntimeException {
    public static final String CUSTOMER_WITH_ID_NOT_FOUND = "Customer with id: %s not found";

    public CustomerNotFoundException(String message) {
        super(message);
    }
}