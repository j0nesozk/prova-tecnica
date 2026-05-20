package br.com.academico.crud.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "student")
public class Student extends Person {

    @Column(name = "student_number", nullable = false, unique = true, length = 60)
    private String studentNumber;

    @Column(name = "photo", length = 500)
    private String photo;

    public Student() {}

    public Student(String name, String phoneNumber, String emailAddress, String studentNumber, String photo) {
        super(name, phoneNumber, emailAddress);
        this.studentNumber = studentNumber;
        this.photo = photo;
    }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
}
