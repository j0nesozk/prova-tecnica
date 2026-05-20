package br.com.academico.crud.service;

import br.com.academico.crud.domain.entity.Student;
import br.com.academico.crud.domain.enums.Status;
import br.com.academico.crud.dto.AddressRequest;
import br.com.academico.crud.dto.StudentRequest;
import br.com.academico.crud.dto.StudentResponse;
import br.com.academico.crud.exception.DuplicateResourceException;
import br.com.academico.crud.exception.ResourceNotFoundException;
import br.com.academico.crud.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    StudentRepository repository;

    @InjectMocks
    StudentService service;

    StudentRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new StudentRequest(
                "Maria Silva",
                "+5511999998888",
                "maria@example.com",
                "STU-2026-001",
                "https://cdn.example.com/p/1.jpg",
                List.of(new AddressRequest("Rua A, 100", "São Paulo", "SP", "01234-567", "Brasil"))
        );
    }

    private Student persisted(Long id, StudentRequest req) {
        Student s = req.toEntity();
        s.setId(id);
        return s;
    }

    @Test
    @DisplayName("create returns persisted student when payload is valid and unique")
    void create_happyPath() {
        when(repository.existsByEmailAddress(validRequest.emailAddress())).thenReturn(false);
        when(repository.existsByStudentNumber(validRequest.studentNumber())).thenReturn(false);
        when(repository.save(any(Student.class))).thenAnswer(inv -> {
            Student s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        StudentResponse resp = service.create(validRequest);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.emailAddress()).isEqualTo("maria@example.com");
        assertThat(resp.status()).isEqualTo(Status.ACTIVE);
        assertThat(resp.addresses()).hasSize(1);
    }

    @Test
    @DisplayName("create throws when email already exists")
    void create_duplicateEmail() {
        when(repository.existsByEmailAddress(validRequest.emailAddress())).thenReturn(true);

        assertThatThrownBy(() -> service.create(validRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create throws when studentNumber already exists")
    void create_duplicateStudentNumber() {
        when(repository.existsByEmailAddress(validRequest.emailAddress())).thenReturn(false);
        when(repository.existsByStudentNumber(validRequest.studentNumber())).thenReturn(true);

        assertThatThrownBy(() -> service.create(validRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Student number");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("findById returns response when found")
    void findById_found() {
        Student s = persisted(7L, validRequest);
        when(repository.findById(7L)).thenReturn(Optional.of(s));

        StudentResponse resp = service.findById(7L);
        assertThat(resp.id()).isEqualTo(7L);
        assertThat(resp.name()).isEqualTo("Maria Silva");
    }

    @Test
    @DisplayName("findById throws when not found")
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findAll only returns ACTIVE when includeDisabled=false")
    void findAll_activeOnly() {
        Pageable p = PageRequest.of(0, 10);
        Page<Student> page = new PageImpl<>(List.of(persisted(1L, validRequest)));
        when(repository.findAllByStatus(Status.ACTIVE, p)).thenReturn(page);

        Page<StudentResponse> result = service.findAll(p, false);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(repository, never()).findAll(p);
    }

    @Test
    @DisplayName("findAll returns all when includeDisabled=true")
    void findAll_includeDisabled() {
        Pageable p = PageRequest.of(0, 10);
        when(repository.findAll(p)).thenReturn(new PageImpl<>(List.of()));

        service.findAll(p, true);

        verify(repository).findAll(p);
        verify(repository, never()).findAllByStatus(any(), any());
    }

    @Test
    @DisplayName("update applies new fields and rejects email collision with another student")
    void update_emailCollision() {
        Student existing = persisted(1L, validRequest);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        StudentRequest changed = new StudentRequest(
                "Maria Silva",
                "+5511999998888",
                "other@example.com",
                "STU-2026-001",
                null,
                null
        );
        when(repository.existsByEmailAddress("other@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, changed))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("update allows keeping same email")
    void update_sameEmail() {
        Student existing = persisted(1L, validRequest);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentResponse resp = service.update(1L, validRequest);

        assertThat(resp.emailAddress()).isEqualTo("maria@example.com");
        verify(repository, never()).existsByEmailAddress(any());
    }

    @Test
    @DisplayName("softDelete flips status to DISABLE")
    void softDelete_marksDisabled() {
        Student existing = persisted(1L, validRequest);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        service.softDelete(1L);

        assertThat(existing.getStatus()).isEqualTo(Status.DISABLE);
    }

    @Test
    @DisplayName("softDelete throws when not found")
    void softDelete_notFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.softDelete(42L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("restore flips status back to ACTIVE")
    void restore_marksActive() {
        Student existing = persisted(1L, validRequest);
        existing.setStatus(Status.DISABLE);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentResponse resp = service.restore(1L);

        assertThat(resp.status()).isEqualTo(Status.ACTIVE);
    }
}
