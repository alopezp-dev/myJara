package com.myjara.backend.dto;

import com.myjara.backend.entity.ClinicalNote;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ClinicalNoteResponse {

    private Long id;
    private Long encounterId;
    private Long professionalId;
    private String professionalName;
    private String type;
    private String content;
    private LocalDateTime createdAt;

    public static ClinicalNoteResponse from(ClinicalNote n) {
        ClinicalNoteResponse r = new ClinicalNoteResponse();
        r.id = n.getId();
        r.encounterId = n.getEncounter().getId();
        r.professionalId = n.getProfessional().getId();
        r.professionalName = n.getProfessional().getFirstName() + " " + n.getProfessional().getLastName();
        r.type = n.getType().name();
        r.content = n.getContent();
        r.createdAt = n.getCreatedAt();
        return r;
    }
}