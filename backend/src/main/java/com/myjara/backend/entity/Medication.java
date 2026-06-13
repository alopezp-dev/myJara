package com.myjara.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "national_code", unique = true, nullable = false, length = 20)
    private String nationalCode;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "active_ingredient", length = 255)
    private String activeIngredient;

    @Column(name = "pharmaceutical_form", length = 100)
    private String pharmaceuticalForm;

    @Column(length = 100)
    private String dosage;

    @Column(length = 50)
    private String route;

    @Column(name = "requires_prescription")
    private Boolean requiresPrescription = true;

    @Column(nullable = false)
    private Boolean active = true;
}