package com.myjara.backend.repository;

import com.myjara.backend.entity.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, Long> {

    List<Agenda> findByProfessionalIdAndActiveTrue(Long professionalId);
    List<Agenda> findByDayOfWeekAndActiveTrue(Integer dayOfWeek);
}