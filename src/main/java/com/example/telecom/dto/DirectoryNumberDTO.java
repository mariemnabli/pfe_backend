package com.example.telecom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectoryNumberDTO {
    private Long id;
    private Long numero;
    private String status;
    private LocalDate dateActivation;
    private LocalDate dateDesactivation;
    private Long contratId;
    private String contractId;
    private Long clientId;
    private String clientNom;
    private String clientPrenom;
    private Long customerGroupId;
    private String customerGroupName;
}
