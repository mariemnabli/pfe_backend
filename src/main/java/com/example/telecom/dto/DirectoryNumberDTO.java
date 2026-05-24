package com.example.telecom.dto;

import com.example.telecom.config.FlexibleLocalDateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectoryNumberDTO {
    private Long id;
    private Long numero;
    private String status;
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime dateActivation;
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime dateDesactivation;
    private Long contratId;
    private String contractId;
    private Long clientId;
    private String clientNom;
    private String clientPrenom;
    private Long customerGroupId;
    private String customerGroupName;
}
