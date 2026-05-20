package br.com.academico.crud.dto;

import br.com.academico.crud.domain.entity.Professor;
import br.com.academico.crud.domain.enums.Status;

import java.math.BigDecimal;
import java.util.List;

public record ProfessorResponse(
        Long id,
        String name,
        String phoneNumber,
        String emailAddress,
        Status status,
        BigDecimal salary,
        List<AddressResponse> addresses
) {
    public static ProfessorResponse from(Professor p) {
        List<AddressResponse> addrs = p.getAddresses().stream()
                .map(AddressResponse::from)
                .toList();
        return new ProfessorResponse(
                p.getId(),
                p.getName(),
                p.getPhoneNumber(),
                p.getEmailAddress(),
                p.getStatus(),
                p.getSalary(),
                addrs
        );
    }
}
