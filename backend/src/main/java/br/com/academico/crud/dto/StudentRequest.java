package br.com.academico.crud.dto;

import br.com.academico.crud.domain.entity.Student;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StudentRequest(
        @NotBlank @Size(min = 2, max = 120) String name,
        @NotBlank @Size(max = 30) String phoneNumber,
        @NotBlank @Email @Size(max = 180) String emailAddress,
        @NotBlank @Size(max = 60) String studentNumber,
        @Size(max = 500) String photo,
        @Valid List<AddressRequest> addresses
) {
    public Student toEntity() {
        Student s = new Student(name, phoneNumber, emailAddress, studentNumber, photo);
        if (addresses != null) {
            for (AddressRequest ar : addresses) {
                s.addAddress(ar.toEntity());
            }
        }
        return s;
    }

    public void applyTo(Student s) {
        s.setName(name);
        s.setPhoneNumber(phoneNumber);
        s.setEmailAddress(emailAddress);
        s.setStudentNumber(studentNumber);
        s.setPhoto(photo);
    }
}
