package com.myjara.backend.service;

import com.myjara.backend.dto.*;
import com.myjara.backend.entity.*;
import com.myjara.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final EmergencyEpisodeRepository episodeRepository;
    private final EmergencyBoxRepository boxRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;

    // Colores del triaje Manchester
    private static final Map<Integer, String> TRIAGE_COLORS = Map.of(
            1, "RED",
            2, "ORANGE",
            3, "YELLOW",
            4, "GREEN",
            5, "BLUE"
    );

    @Transactional(readOnly = true)
    public List<EmergencyEpisodeResponse> findActive() {
        return episodeRepository.findActiveOrderByTriage()
                .stream().map(EmergencyEpisodeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EmergencyEpisodeResponse> findByPatient(Long patientId) {
        return episodeRepository.findByPatientIdOrderByArrivalTimeDesc(patientId)
                .stream().map(EmergencyEpisodeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EmergencyEpisodeResponse findById(Long id) {
        EmergencyEpisode e = episodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Episodio no encontrado: " + id));
        return EmergencyEpisodeResponse.from(e);
    }

    @Transactional(readOnly = true)
    public List<EmergencyBox> findBoxes() {
        return boxRepository.findByActiveTrue();
    }

    @Transactional
    public EmergencyEpisodeResponse registerArrival(EmergencyEpisodeRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        EmergencyEpisode episode = new EmergencyEpisode();
        episode.setPatient(patient);
        episode.setTriageLevel(req.getTriageLevel());
        episode.setTriageColor(TRIAGE_COLORS.get(req.getTriageLevel()));
        episode.setChiefComplaint(req.getChiefComplaint());
        episode.setTriageTime(LocalDateTime.now());

        return EmergencyEpisodeResponse.from(episodeRepository.save(episode));
    }

    @Transactional
    public EmergencyEpisodeResponse assignBox(Long episodeId, Long boxId, Long professionalId) {
        EmergencyEpisode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Episodio no encontrado"));

        EmergencyBox box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box no encontrado"));

        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        episode.setBox(box);
        episode.setProfessional(professional);
        episode.setStatus(EmergencyEpisode.Status.IN_ATTENTION);
        episode.setAttentionTime(LocalDateTime.now());

        box.setStatus(EmergencyBox.Status.OCCUPIED);
        boxRepository.save(box);

        return EmergencyEpisodeResponse.from(episodeRepository.save(episode));
    }

    @Transactional
    public EmergencyEpisodeResponse discharge(Long episodeId, String dischargeType, String notes) {
        EmergencyEpisode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Episodio no encontrado"));

        episode.setStatus(EmergencyEpisode.Status.DISCHARGED);
        episode.setDischargeTime(LocalDateTime.now());
        episode.setDischargeType(EmergencyEpisode.DischargeType.valueOf(dischargeType));
        if (notes != null) episode.setNotes(notes);

        if (episode.getBox() != null) {
            episode.getBox().setStatus(EmergencyBox.Status.CLEANING);
            boxRepository.save(episode.getBox());
        }

        return EmergencyEpisodeResponse.from(episodeRepository.save(episode));
    }

    @Transactional
    public VitalSignsResponse recordVitalSigns(VitalSignsRequest req) {
        EmergencyEpisode episode = episodeRepository.findById(req.getEpisodeId())
                .orElseThrow(() -> new RuntimeException("Episodio no encontrado"));

        Professional professional = professionalRepository.findById(req.getProfessionalId())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        VitalSigns vs = new VitalSigns();
        vs.setEpisode(episode);
        vs.setProfessional(professional);
        vs.setSystolicBp(req.getSystolicBp());
        vs.setDiastolicBp(req.getDiastolicBp());
        vs.setHeartRate(req.getHeartRate());
        vs.setRespiratoryRate(req.getRespiratoryRate());
        vs.setTemperature(req.getTemperature());
        vs.setOxygenSaturation(req.getOxygenSaturation());
        vs.setPainScale(req.getPainScale());

        return VitalSignsResponse.from(vitalSignsRepository.save(vs));
    }

    @Transactional(readOnly = true)
    public List<VitalSignsResponse> findVitalSigns(Long episodeId) {
        return vitalSignsRepository.findByEpisodeIdOrderByRecordedAtDesc(episodeId)
                .stream().map(VitalSignsResponse::from).toList();
    }
}