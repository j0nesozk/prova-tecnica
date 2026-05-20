package br.com.academico.crud.service;

import br.com.academico.crud.domain.entity.Student;
import br.com.academico.crud.domain.enums.Status;
import br.com.academico.crud.dto.AddressRequest;
import br.com.academico.crud.dto.StudentRequest;
import br.com.academico.crud.dto.StudentResponse;
import br.com.academico.crud.exception.DuplicateResourceException;
import br.com.academico.crud.exception.ResourceNotFoundException;
import br.com.academico.crud.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class StudentService {

    private final StudentRepository repository;
    private final PhotoStorageService photoStorage;

    public StudentService(StudentRepository repository, PhotoStorageService photoStorage) {
        this.repository = repository;
        this.photoStorage = photoStorage;
    }

    public StudentResponse create(StudentRequest req) {
        if (repository.existsByEmailAddress(req.emailAddress())) {
            throw new DuplicateResourceException("Email already in use: " + req.emailAddress());
        }
        if (repository.existsByStudentNumber(req.studentNumber())) {
            throw new DuplicateResourceException("Student number already in use: " + req.studentNumber());
        }
        Student saved = repository.save(req.toEntity());
        return StudentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return repository.findById(id)
                .map(StudentResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> findAll(Pageable pageable, boolean includeDisabled) {
        Page<Student> page = includeDisabled
                ? repository.findAll(pageable)
                : repository.findAllByStatus(Status.ACTIVE, pageable);
        return page.map(StudentResponse::from);
    }

    public StudentResponse update(Long id, StudentRequest req) {
        Student existing = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));

        if (!existing.getEmailAddress().equals(req.emailAddress())
                && repository.existsByEmailAddress(req.emailAddress())) {
            throw new DuplicateResourceException("Email already in use: " + req.emailAddress());
        }
        if (!existing.getStudentNumber().equals(req.studentNumber())
                && repository.existsByStudentNumber(req.studentNumber())) {
            throw new DuplicateResourceException("Student number already in use: " + req.studentNumber());
        }

        req.applyTo(existing);
        existing.clearAddresses();
        if (req.addresses() != null) {
            for (AddressRequest ar : req.addresses()) {
                existing.addAddress(ar.toEntity());
            }
        }
        return StudentResponse.from(repository.save(existing));
    }

    public void softDelete(Long id) {
        Student existing = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));
        existing.setStatus(Status.DISABLE);
        repository.save(existing);
    }

    public StudentResponse restore(Long id) {
        Student existing = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));
        existing.setStatus(Status.ACTIVE);
        return StudentResponse.from(repository.save(existing));
    }

    public StudentResponse uploadPhoto(Long id, MultipartFile file) {
        Student existing = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));
        String oldPhoto = existing.getPhoto();
        String newPath = photoStorage.store(file);
        existing.setPhoto(newPath);
        Student saved = repository.save(existing);
        photoStorage.delete(oldPhoto);
        return StudentResponse.from(saved);
    }

    public StudentResponse deletePhoto(Long id) {
        Student existing = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));
        String oldPhoto = existing.getPhoto();
        existing.setPhoto(null);
        Student saved = repository.save(existing);
        photoStorage.delete(oldPhoto);
        return StudentResponse.from(saved);
    }
}
