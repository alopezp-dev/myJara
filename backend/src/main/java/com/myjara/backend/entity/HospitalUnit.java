package com.myjara.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hospital_units")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HospitalUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 10)
    private String floor;

    @Column(name = "beds_total", nullable = false)
    private Integer bedsTotal = 0;

    @Column(length = 150)
    private String center;

    @Column(nullable = false)
    private Boolean active = true;
}