package com.myjara.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "emergency_boxes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.FREE;

    @Column(length = 150)
    private String center;

    @Column(nullable = false)
    private Boolean active = true;

    public enum Type {
        GENERAL,
        RESUSCITATION,
        OBSERVATION,
        TRIAGE
    }

    public enum Status {
        FREE,
        OCCUPIED,
        CLEANING
    }
}