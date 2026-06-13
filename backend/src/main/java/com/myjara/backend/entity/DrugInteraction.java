package com.myjara.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "drug_interactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DrugInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_a_id", nullable = false)
    private Medication medicationA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_b_id", nullable = false)
    private Medication medicationB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity = Severity.MODERATE;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    public enum Severity {
        MILD,
        MODERATE,
        SEVERE
    }
}