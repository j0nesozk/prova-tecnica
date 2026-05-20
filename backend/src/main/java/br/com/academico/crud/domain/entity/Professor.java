package br.com.academico.crud.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "professor")
public class Professor extends Person {

    @Column(name = "salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    public Professor() {}

    public Professor(String name, String phoneNumber, String emailAddress, BigDecimal salary) {
        super(name, phoneNumber, emailAddress);
        this.salary = salary;
    }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
}
