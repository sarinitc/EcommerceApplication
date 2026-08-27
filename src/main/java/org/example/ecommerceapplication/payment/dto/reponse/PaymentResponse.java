package org.example.ecommerceapplication.payment.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.ecommerceapplication.payment.entity.PaymentMethod;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private  Long paymentId;
    private PaymentMethod paymentMethod;
}
