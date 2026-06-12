package com.myjara.backend.repository;

import com.myjara.backend.entity.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalRepository extends JpaRepository<Professional, Long> {

    Optional<Professional> findByLicenseNumber(String licenseNumber);
    List<Professional> findByActiveTrue();
    List<Professional> findBySpecialtyAndActiveTrue(String specialty);
}