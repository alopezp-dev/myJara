package com.myjara.backend.repository;

import com.myjara.backend.entity.EmergencyEpisode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyEpisodeRepository extends JpaRepository<EmergencyEpisode, Long> {

    List<EmergencyEpisode> findByPatientIdOrderByArrivalTimeDesc(Long patientId);

    // Episodios activos ordenados por nivel de triaje (más urgente primero)
    @Query("SELECT e FROM EmergencyEpisode e WHERE " +
            "e.status NOT IN ('DISCHARGED', 'ADMITTED') " +
            "ORDER BY e.triageLevel ASC, e.arrivalTime ASC")
    List<EmergencyEpisode> findActiveOrderByTriage();

    List<EmergencyEpisode> findByStatus(EmergencyEpisode.Status status);
}