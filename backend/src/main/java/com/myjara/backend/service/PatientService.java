package com.myjara.backend.service;

import com.myjara.backend.dto.PatientRequest;
import com.myjara.backend.dto.PatientResponse;
import com.myjara.backend.entity.Patient;
import com.myjara.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public List<PatientResponse> findAll() {
        return patientRepository.findByActiveTrue()
                .stream()
                .map(PatientResponse::from)
                .toList();
    }

    public PatientResponse findById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + id));
        return PatientResponse.from(patient);
    }

    public List<PatientResponse> search(String term) {
        return patientRepository.search(term)
                .stream()
                .map(PatientResponse::from)
                .toList();
    }

    @Transactional
    public PatientResponse create(PatientRequest req) {
        Patient patient = new Patient();
        patient.setHealthCard(req.getHealthCard());
        patient.setDni(req.getDni());
        patient.setFirstName(req.getFirstName());
        patient.setLastName(req.getLastName());
        patient.setBirthDate(req.getBirthDate());
        if (req.getGender() != null) {
            patient.setGender(Patient.Gender.valueOf(req.getGender()));
        }
        patient.setPhone(req.getPhone());
        patient.setEmail(req.getEmail());
        patient.setAddress(req.getAddress());
        patient.setMunicipality(req.getMunicipality());
        return PatientResponse.from(patientRepository.save(patient));
    }

    @Transactional
    public PatientResponse update(Long id, PatientRequest req) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + id));
        patient.setFirstName(req.getFirstName());
        patient.setLastName(req.getLastName());
        patient.setPhone(req.getPhone());
        patient.setEmail(req.getEmail());
        patient.setAddress(req.getAddress());
        patient.setMunicipality(req.getMunicipality());
        return PatientResponse.from(patientRepository.save(patient));
    }

    @Transactional
    public void delete(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + id));
        patient.setActive(false); // Baja lógica — nunca se borran datos sanitarios
        patientRepository.save(patient);
    }
}