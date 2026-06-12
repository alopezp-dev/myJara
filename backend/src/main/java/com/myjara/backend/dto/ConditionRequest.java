package com.myjara.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class ConditionRequest {

    @NotNull
    private Long encounterId;

    @NotNull
    private Long patientId;

    @NotBlank
    private String cie10Code;

    @NotBlank
    private String cie10Desc;

    private String status;
    private LocalDate onsetDate;
    private String notes;
}