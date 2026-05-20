package br.com.academico.crud.dto;

import br.com.academico.crud.domain.entity.Professor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProfessorRequest(
        @NotBlank @Size(min = 2, max = 120) String name,
        @NotBlank @Size(max = 30) String phoneNumber,
        @NotBlank @Email @Size(max = 180) String emailAddress,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal salary,
        @Valid List<AddressRequest> addresses
) {
    public Professor toEntity() {
        Professor p = new Professor(name, phoneNumber, emailAddress, salary);
        if (addresses != null) {
            for (AddressRequest ar : addresses) {
                p.addAddress(ar.toEntity());
            }
        }
        return p;
    }

    public void applyTo(Professor p) {
        p.setName(name);
        p.setPhoneNumber(phoneNumber);
        p.setEmailAddress(emailAddress);
        p.setSalary(salary);
    }
}
