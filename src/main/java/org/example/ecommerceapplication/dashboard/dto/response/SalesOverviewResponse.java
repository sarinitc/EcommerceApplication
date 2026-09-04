package org.example.ecommerceapplication.dashboard.dto.response;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOverviewResponse {

    private LocalDate date;

    private long orders;

    private BigDecimal revenue;
}