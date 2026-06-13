package com.myjara.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class VitalSignsRequest {

    @NotNull
    private Long episodeId;

    @NotNull
    private Long professionalId;

    private Integer systolicBp;
    private Integer diastolicBp;
    private Integer heartRate;
    private Integer respiratoryRate;
    private BigDecimal temperature;
    private Integer oxygenSaturation;
    private Integer painScale;
}