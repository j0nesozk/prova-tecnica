package br.com.academico.crud.repository;

import br.com.academico.crud.domain.entity.Professor;
import br.com.academico.crud.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    Page<Professor> findAllByStatus(Status status, Pageable pageable);

    Optional<Professor> findByEmailAddress(String emailAddress);

    boolean existsByEmailAddress(String emailAddress);
}
