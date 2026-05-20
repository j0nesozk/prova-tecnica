package br.com.academico.crud.dto;

import br.com.academico.crud.domain.entity.Student;
import br.com.academico.crud.domain.enums.Status;

import java.util.List;

public record StudentResponse(
        Long id,
        String name,
        String phoneNumber,
        String emailAddress,
        Status status,
        String studentNumber,
        String photo,
        List<AddressResponse> addresses
) {
    public static StudentResponse from(Student s) {
        List<AddressResponse> addrs = s.getAddresses().stream()
                .map(AddressResponse::from)
                .toList();
        return new StudentResponse(
                s.getId(),
                s.getName(),
                s.getPhoneNumber(),
                s.getEmailAddress(),
                s.getStatus(),
                s.getStudentNumber(),
                s.getPhoto(),
                addrs
        );
    }
}
