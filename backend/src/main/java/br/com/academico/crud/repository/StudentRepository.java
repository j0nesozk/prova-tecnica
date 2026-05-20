package br.com.academico.crud.repository;

import br.com.academico.crud.domain.entity.Student;
import br.com.academico.crud.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Page<Student> findAllByStatus(Status status, Pageable pageable);

    Optional<Student> findByEmailAddress(String emailAddress);

    Optional<Student> findByStudentNumber(String studentNumber);

    boolean existsByEmailAddress(String emailAddress);

    boolean existsByStudentNumber(String studentNumber);
}
