package com.example.telecom.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkCustomerDateUpdateDTO {
    private Long promotionId;
    private Long groupId;
    private List<Long> customerIds;
    private LocalDate newStartDate;
    private LocalDate newEndDate;
}