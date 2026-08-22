package org.example.ecommerceapplication.product.exception;

public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String productName) {
        super("Product already exists: " + productName);
    }
}
