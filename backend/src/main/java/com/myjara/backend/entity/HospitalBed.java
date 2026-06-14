package com.myjara.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hospital_beds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HospitalBed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private HospitalUnit unit;

    @Column(name = "bed_code", unique = true, nullable = false, length = 20)
    private String bedCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.FREE;

    @Column(nullable = false)
    private Boolean active = true;

    public enum Status {
        FREE,
        OCCUPIED,
        CLEANING,
        RESERVED
    }
}