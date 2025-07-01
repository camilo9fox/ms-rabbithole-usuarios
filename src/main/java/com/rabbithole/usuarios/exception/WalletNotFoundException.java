package com.rabbithole.usuarios.exception;

public class WalletNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public WalletNotFoundException(String message) {
        super(message);
    }
    
    public WalletNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
