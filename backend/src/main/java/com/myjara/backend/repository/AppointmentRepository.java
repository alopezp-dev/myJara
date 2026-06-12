package com.myjara.backend.repository;

import com.myjara.backend.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByProfessionalIdAndStartTimeBetween(
            Long professionalId,
            LocalDateTime from,
            LocalDateTime to);

    // Comprueba si hay solapamiento de citas para un profesional
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE " +
            "a.professional.id = :professionalId AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW') AND " +
            "a.startTime < :endTime AND a.endTime > :startTime")
    boolean existsOverlap(
            @Param("professionalId") Long professionalId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<Appointment> findByPatientIdAndStatus(
            Long patientId,
            Appointment.Status status);
}