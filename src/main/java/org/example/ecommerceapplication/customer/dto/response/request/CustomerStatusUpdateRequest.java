package org.example.ecommerceapplication.customer.dto.response.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.ecommerceapplication.user.entity.AccountStatus;

@Getter
@Setter
public class CustomerStatusUpdateRequest {

    @NotNull(message = "status is required")
    private AccountStatus status;
}
