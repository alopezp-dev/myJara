package com.myjara.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProfessionalRequest {

    @NotBlank @Size(max = 100)
    private String firstName;

    @NotBlank @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String specialty;

    @NotBlank @Size(max = 50)
    private String licenseNumber;

    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phone;
}