package com.myjara.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "agendas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    @Column(nullable = false, length = 150)
    private String center;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;   // 1=Lunes ... 7=Domingo

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_minutes", nullable = false)
    private Integer slotMinutes = 15;

    @Column(nullable = false)
    private Boolean active = true;
}