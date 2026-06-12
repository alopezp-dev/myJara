package com.myjara.backend.dto;

import com.myjara.backend.entity.Patient;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class PatientResponse {

    private Long id;
    private String healthCard;
    private String dni;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    private String phone;
    private String email;
    private String address;
    private String municipality;
    private Boolean active;

    // Convierte una entidad Patient a PatientResponse
    public static PatientResponse from(Patient p) {
        PatientResponse r = new PatientResponse();
        r.id = p.getId();
        r.healthCard = p.getHealthCard();
        r.dni = p.getDni();
        r.firstName = p.getFirstName();
        r.lastName = p.getLastName();
        r.birthDate = p.getBirthDate();
        r.gender = p.getGender() != null ? p.getGender().name() : null;
        r.phone = p.getPhone();
        r.email = p.getEmail();
        r.address = p.getAddress();
        r.municipality = p.getMunicipality();
        r.active = p.getActive();
        return r;
    }
}