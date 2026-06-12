package com.myjara.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cie10_catalog")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cie10Catalog {

    @Id
    @Column(length = 10)
    private String code;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(length = 100)
    private String category;
}