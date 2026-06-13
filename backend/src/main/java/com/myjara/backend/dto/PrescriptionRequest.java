package com.myjara.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class PrescriptionRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private Long professionalId;

    private Long encounterId;

    @NotNull
    private Long medicationId;

    @NotBlank
    private String dose;

    @NotBlank
    private String frequency;

    private String duration;
    private String route;
    private String instructions;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}