package com.myjara.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_episodes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyEpisode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "triage_level", nullable = false)
    private Integer triageLevel;

    @Column(name = "triage_color", nullable = false, length = 20)
    private String triageColor;

    @Column(name = "chief_complaint", nullable = false, length = 255)
    private String chiefComplaint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.WAITING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id")
    private EmergencyBox box;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id")
    private Professional professional;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "triage_time")
    private LocalDateTime triageTime;

    @Column(name = "attention_time")
    private LocalDateTime attentionTime;

    @Column(name = "discharge_time")
    private LocalDateTime dischargeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "discharge_type", length = 30)
    private DischargeType dischargeType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (arrivalTime == null) arrivalTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status {
        WAITING,        // En espera
        TRIAGE,         // En triaje
        IN_ATTENTION,   // En atención
        OBSERVATION,    // En observación
        DISCHARGED,     // Dado de alta
        ADMITTED        // Ingresado
    }

    public enum DischargeType {
        HOME,           // Alta a domicilio
        ADMISSION,      // Ingreso hospitalario
        TRANSFER,       // Traslado
        DEATH,          // Éxitus
        VOLUNTARY       // Alta voluntaria
    }
}