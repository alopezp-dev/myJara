package com.myjara.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AdmissionRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private Long bedId;

    @NotNull
    private Long professionalId;

    private Long emergencyEpisodeId;

    private String admissionType;

    @NotBlank
    private String reason;

    private String diagnosis;
}