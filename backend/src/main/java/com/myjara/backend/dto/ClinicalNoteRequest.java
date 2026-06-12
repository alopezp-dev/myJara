package com.myjara.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClinicalNoteRequest {

    @NotNull
    private Long encounterId;

    @NotNull
    private Long professionalId;

    private String type;

    @NotBlank
    private String content;
}