package com.myjara.backend.repository;

import com.myjara.backend.entity.HospitalBed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalBedRepository extends JpaRepository<HospitalBed, Long> {
    List<HospitalBed> findByUnitIdAndActiveTrue(Long unitId);
    List<HospitalBed> findByStatusAndActiveTrue(HospitalBed.Status status);
    List<HospitalBed> findByUnitIdAndStatusAndActiveTrue(Long unitId, HospitalBed.Status status);
}