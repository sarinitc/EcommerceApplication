package org.example.ecommerceapplication.customer.service;

import jakarta.validation.Validation;
import org.example.ecommerceapplication.customer.dto.request.AdminCustomerUpdateRequest;
import org.example.ecommerceapplication.customer.dto.response.request.AdminCreateCustomerRequest;
import org.example.ecommerceapplication.auth.dto.request.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerValidationTest {
    @Test
    void optionalFieldsStayOptionalButRejectOversizeAndBrowserOnlyImages() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var patch = new AdminCustomerUpdateRequest();
            assertTrue(validator.validate(patch).isEmpty());
            patch.setFirstName("x".repeat(101));
            patch.setProfileImage("blob:http://localhost:3000/example");
            assertEquals(2, validator.validate(patch).size());
            patch.setFirstName(null);
            for (String image : new String[]{null, "/uploads/profiles/example.jpg", "https://cdn.example.com/image.jpg"}) {
                patch.setProfileImage(image);
                assertTrue(validator.validate(patch).isEmpty());
            }
            var create = new AdminCreateCustomerRequest();
            create.setUsername("customer");
            create.setEmail("customer@example.com");
            create.setTemporaryPassword("Temporary123!");
            assertTrue(validator.validate(create).isEmpty());
            create.setProfileImage("BLOB:http://localhost:3000/example");
            assertEquals(1, validator.validate(create).size());
        }
    }

    @Test
    void requestToStringDoesNotExposePasswordsOrOtps() {
        var register = new RegisterRequest();
        register.setPassword("secret-value");
        var login = new LoginRequest();
        login.setPassword("secret-value");
        var reset = new ResetPasswordRequest();
        reset.setNewPassword("secret-value");
        reset.setOtp("123456");
        var verify = new VerifyOtpRequest();
        verify.setOtp("123456");
        var create = new AdminCreateCustomerRequest();
        create.setTemporaryPassword("secret-value");
        for (Object request : new Object[]{register, login, reset, verify, create}) {
            assertFalse(request.toString().contains("secret-value"));
            assertFalse(request.toString().contains("123456"));
        }
    }
}
