package org.example.ecommerceapplication.common.exception;

import org.example.ecommerceapplication.category.exception.CategoryAlreadyExistsException;
import org.example.ecommerceapplication.address.exception.AddressAlreadyExistsException;
import org.example.ecommerceapplication.auth.exception.OtpException;
import org.example.ecommerceapplication.orders.exception.OrderNotFoundException;
import org.example.ecommerceapplication.product.exception.ProductAlreadyExistsException;
import org.example.ecommerceapplication.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionhandler {

    @ExceptionHandler(OtpException.class)
    public ResponseEntity<ApiResponse<Void>> handleOtpException(
            OtpException exception
    ) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(exception.getMessage())
                .status(exception.getStatus().value())
                .payload(null)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderNotFound(
            OrderNotFoundException exception
    ) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(exception.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .payload(null)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryAlreadyExists(
            CategoryAlreadyExistsException exception
    ) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(exception.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .payload(null)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(AddressAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleAddressAlreadyExists(
            AddressAlreadyExistsException exception
    ) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(exception.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .payload(null)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductAlreadyExists(
            ProductAlreadyExistsException exception
    ) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(exception.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .payload(null)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
