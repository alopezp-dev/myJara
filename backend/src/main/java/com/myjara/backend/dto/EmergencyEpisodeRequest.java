package com.myjara.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmergencyEpisodeRequest {

    @NotNull
    private Long patientId;

    @NotNull
    @Min(1) @Max(5)
    private Integer triageLevel;

    @NotBlank
    private String chiefComplaint;
}