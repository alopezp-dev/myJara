package com.myjara.backend.dto;

import com.myjara.backend.entity.DrugInteraction;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InteractionWarning {

    private Long interactionId;
    private String medicationA;
    private String medicationB;
    private String severity;
    private String description;

    public static InteractionWarning from(DrugInteraction i) {
        InteractionWarning w = new InteractionWarning();
        w.interactionId = i.getId();
        w.medicationA = i.getMedicationA().getName();
        w.medicationB = i.getMedicationB().getName();
        w.severity = i.getSeverity().name();
        w.description = i.getDescription();
        return w;
    }
}