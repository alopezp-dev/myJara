package com.myjara.backend.dto;

import com.myjara.backend.entity.Professional;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProfessionalResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String specialty;
    private String licenseNumber;
    private String email;
    private String phone;
    private Boolean active;

    public static ProfessionalResponse from(Professional p) {
        ProfessionalResponse r = new ProfessionalResponse();
        r.id = p.getId();
        r.firstName = p.getFirstName();
        r.lastName = p.getLastName();
        r.specialty = p.getSpecialty();
        r.licenseNumber = p.getLicenseNumber();
        r.email = p.getEmail();
        r.phone = p.getPhone();
        r.active = p.getActive();
        return r;
    }
}