package com.myjara.backend.repository;

import com.myjara.backend.entity.DrugInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugInteractionRepository extends JpaRepository<DrugInteraction, Long> {

    // Busca interacciones entre el nuevo medicamento y cualquier medicamento activo
    @Query("SELECT i FROM DrugInteraction i WHERE " +
            "(i.medicationA.id = :medId OR i.medicationB.id = :medId) AND " +
            "(i.medicationA.id IN :activeMedIds OR i.medicationB.id IN :activeMedIds)")
    List<DrugInteraction> findInteractions(
            @Param("medId") Long medId,
            @Param("activeMedIds") List<Long> activeMedIds);
}