package org.example.ecommerceapplication.dashboard.controller;


import lombok.RequiredArgsConstructor;

import org.example.ecommerceapplication.common.response.ApiResponse;
import org.example.ecommerceapplication.dashboard.dto.response.DashboardOverviewResponse;
import org.example.ecommerceapplication.dashboard.service.DashboardService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>>
    getOverview(
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate to
    ) {

        DashboardOverviewResponse dashboard =
                dashboardService.getOverview(
                        from,
                        to
                );


        ApiResponse<DashboardOverviewResponse> response =
                ApiResponse
                        .<DashboardOverviewResponse>builder()
                        .success(true)
                        .message(
                                "Dashboard overview retrieved successfully"
                        )
                        .status(200)
                        .payload(dashboard)
                        .timestamp(
                                Instant.now()
                        )
                        .build();
        return ResponseEntity.ok(response);
    }
}