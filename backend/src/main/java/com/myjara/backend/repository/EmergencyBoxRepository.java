package com.myjara.backend.repository;

import com.myjara.backend.entity.EmergencyBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyBoxRepository extends JpaRepository<EmergencyBox, Long> {

    List<EmergencyBox> findByActiveTrue();
    List<EmergencyBox> findByStatusAndActiveTrue(EmergencyBox.Status status);
    List<EmergencyBox> findByTypeAndActiveTrue(EmergencyBox.Type type);
}