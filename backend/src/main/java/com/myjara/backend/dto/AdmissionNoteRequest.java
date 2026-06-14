package com.myjara.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AdmissionNoteRequest {

    @NotNull
    private Long admissionId;

    @NotNull
    private Long professionalId;

    private String noteType;

    @NotBlank
    private String content;
}