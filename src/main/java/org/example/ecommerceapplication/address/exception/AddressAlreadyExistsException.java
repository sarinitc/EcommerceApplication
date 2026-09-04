package org.example.ecommerceapplication.address.exception;

public class AddressAlreadyExistsException extends RuntimeException {

    public AddressAlreadyExistsException() {
        super("This address already exists for the current user");
    }
}
