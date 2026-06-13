package com.myjara.backend.dto;

import com.myjara.backend.entity.VitalSigns;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class VitalSignsResponse {

    private Long id;
    private Long episodeId;
    private String professionalName;
    private Integer systolicBp;
    private Integer diastolicBp;
    private Integer heartRate;
    private Integer respiratoryRate;
    private BigDecimal temperature;
    private Integer oxygenSaturation;
    private Integer painScale;
    private LocalDateTime recordedAt;

    public static VitalSignsResponse from(VitalSigns v) {
        VitalSignsResponse r = new VitalSignsResponse();
        r.id = v.getId();
        r.episodeId = v.getEpisode().getId();
        r.professionalName = v.getProfessional().getFirstName() + " " +
                v.getProfessional().getLastName();
        r.systolicBp = v.getSystolicBp();
        r.diastolicBp = v.getDiastolicBp();
        r.heartRate = v.getHeartRate();
        r.respiratoryRate = v.getRespiratoryRate();
        r.temperature = v.getTemperature();
        r.oxygenSaturation = v.getOxygenSaturation();
        r.painScale = v.getPainScale();
        r.recordedAt = v.getRecordedAt();
        return r;
    }
}