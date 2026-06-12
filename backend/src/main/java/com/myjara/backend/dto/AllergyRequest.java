package com.myjara.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class AllergyRequest {

    @NotNull
    private Long patientId;

    @NotBlank
    private String substance;

    private String reaction;
    private String severity;
    private LocalDate onsetDate;
}