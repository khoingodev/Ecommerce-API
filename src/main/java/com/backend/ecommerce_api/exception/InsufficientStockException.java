package com.backend.ecommerce_api.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException() {
        super("Out of stock or insufficient quantity");
    }
}
