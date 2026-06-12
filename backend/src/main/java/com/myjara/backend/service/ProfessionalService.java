package com.myjara.backend.service;

import com.myjara.backend.dto.ProfessionalRequest;
import com.myjara.backend.dto.ProfessionalResponse;
import com.myjara.backend.entity.Professional;
import com.myjara.backend.repository.ProfessionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;

    public List<ProfessionalResponse> findAll() {
        return professionalRepository.findByActiveTrue()
                .stream().map(ProfessionalResponse::from).toList();
    }

    public ProfessionalResponse findById(Long id) {
        Professional p = professionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado: " + id));
        return ProfessionalResponse.from(p);
    }

    public List<ProfessionalResponse> findBySpecialty(String specialty) {
        return professionalRepository.findBySpecialtyAndActiveTrue(specialty)
                .stream().map(ProfessionalResponse::from).toList();
    }

    @Transactional
    public ProfessionalResponse create(ProfessionalRequest req) {
        Professional p = new Professional();
        p.setFirstName(req.getFirstName());
        p.setLastName(req.getLastName());
        p.setSpecialty(req.getSpecialty());
        p.setLicenseNumber(req.getLicenseNumber());
        p.setEmail(req.getEmail());
        p.setPhone(req.getPhone());
        return ProfessionalResponse.from(professionalRepository.save(p));
    }

    @Transactional
    public void delete(Long id) {
        Professional p = professionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado: " + id));
        p.setActive(false);
        professionalRepository.save(p);
    }
}