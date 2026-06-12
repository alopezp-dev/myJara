package com.myjara.backend.dto;

import com.myjara.backend.entity.Allergy;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class AllergyResponse {

    private Long id;
    private Long patientId;
    private String substance;
    private String reaction;
    private String severity;
    private String status;
    private LocalDate onsetDate;

    public static AllergyResponse from(Allergy a) {
        AllergyResponse r = new AllergyResponse();
        r.id = a.getId();
        r.patientId = a.getPatient().getId();
        r.substance = a.getSubstance();
        r.reaction = a.getReaction();
        r.severity = a.getSeverity().name();
        r.status = a.getStatus().name();
        r.onsetDate = a.getOnsetDate();
        return r;
    }
}