package com.myjara.backend.repository;

import com.myjara.backend.entity.HospitalUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalUnitRepository extends JpaRepository<HospitalUnit, Long> {
    List<HospitalUnit> findByActiveTrue();
}