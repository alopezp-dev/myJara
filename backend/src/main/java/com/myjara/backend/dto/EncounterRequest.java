package com.myjara.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EncounterRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private Long professionalId;

    private Long appointmentId;
    private String type;
    private String reason;
}