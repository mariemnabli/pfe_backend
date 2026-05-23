package com.example.telecom.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDateUpdateResponseDTO {
    private int totalProcessed;
    private int successCount;
    private int failedCount;
    private List<DateUpdateError> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DateUpdateError {
        private Long customerId;
        private String customerName;
        private String errorMessage;
    }
}