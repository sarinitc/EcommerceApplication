package org.example.ecommerceapplication.auth.exception;

import org.springframework.http.HttpStatus;

public class OtpException extends RuntimeException {

    private final HttpStatus status;

    public OtpException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
