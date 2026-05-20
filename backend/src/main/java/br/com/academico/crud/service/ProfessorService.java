package br.com.academico.crud.service;

import br.com.academico.crud.domain.entity.Professor;
import br.com.academico.crud.domain.enums.Status;
import br.com.academico.crud.dto.AddressRequest;
import br.com.academico.crud.dto.ProfessorRequest;
import br.com.academico.crud.dto.ProfessorResponse;
import br.com.academico.crud.exception.DuplicateResourceException;
import br.com.academico.crud.exception.ResourceNotFoundException;
import br.com.academico.crud.repository.ProfessorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfessorService {

    private final ProfessorRepository repository;

    public ProfessorService(ProfessorRepository repository) {
        this.repository = repository;
    }

    public ProfessorResponse create(ProfessorRequest req) {
        if (repository.existsByEmailAddress(req.emailAddress())) {
            throw new DuplicateResourceException("Email already in use: " + req.emailAddress());
        }
        Professor saved = repository.save(req.toEntity());
        return ProfessorResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ProfessorResponse findById(Long id) {
        return repository.findById(id)
                .map(ProfessorResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.of("Professor", id));
    }

    @Transactional(readOnly = true)
    public Page<ProfessorResponse> findAll(Pageable pageable, boolean includeDisabled) {
        Page<Professor> page = includeDisabled
                ? repository.findAll(pageable)
                : repository.findAllByStatus(Status.ACTIVE, pageable);
        return page.map(ProfessorResponse::from);
    }

    public ProfessorResponse update(Long id, ProfessorRequest req) {
        Professor existing = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Professor", id));

        if (!existing.getEmailAddress().equals(req.emailAddress())
                && repository.existsByEmailAddress(req.emailAddress())) {
            throw new DuplicateResourceException("Email already in use: " + req.emailAddress());
        }

        req.applyTo(existing);
        existing.clearAddresses();
        if (req.addresses() != null) {
            for (AddressRequest ar : req.addresses()) {
                existing.addAddress(ar.toEntity());
            }
        }
        return ProfessorResponse.from(repository.save(existing));
    }

    public void softDelete(Long id) {
        Professor existing = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Professor", id));
        existing.setStatus(Status.DISABLE);
        repository.save(existing);
    }

    public ProfessorResponse restore(Long id) {
        Professor existing = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Professor", id));
        existing.setStatus(Status.ACTIVE);
        return ProfessorResponse.from(repository.save(existing));
    }
}
