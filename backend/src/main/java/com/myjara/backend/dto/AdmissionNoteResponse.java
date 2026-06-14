package com.myjara.backend.dto;

import com.myjara.backend.entity.AdmissionNote;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class AdmissionNoteResponse {

    private Long id;
    private Long admissionId;
    private String professionalName;
    private String noteType;
    private String content;
    private LocalDateTime createdAt;

    public static AdmissionNoteResponse from(AdmissionNote n) {
        AdmissionNoteResponse r = new AdmissionNoteResponse();
        r.id = n.getId();
        r.admissionId = n.getAdmission().getId();
        r.professionalName = n.getProfessional().getFirstName() + " " +
                n.getProfessional().getLastName();
        r.noteType = n.getNoteType().name();
        r.content = n.getContent();
        r.createdAt = n.getCreatedAt();
        return r;
    }
}