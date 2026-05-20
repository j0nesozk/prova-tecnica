package br.com.academico.crud.service;

import br.com.academico.crud.domain.entity.Professor;
import br.com.academico.crud.domain.enums.Status;
import br.com.academico.crud.dto.ProfessorRequest;
import br.com.academico.crud.dto.ProfessorResponse;
import br.com.academico.crud.exception.DuplicateResourceException;
import br.com.academico.crud.exception.ResourceNotFoundException;
import br.com.academico.crud.repository.ProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessorServiceTest {

    @Mock
    ProfessorRepository repository;

    @InjectMocks
    ProfessorService service;

    ProfessorRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new ProfessorRequest(
                "João Souza",
                "+5511777776666",
                "joao@example.com",
                new BigDecimal("8500.00"),
                null
        );
    }

    private Professor persisted(Long id) {
        Professor p = validRequest.toEntity();
        p.setId(id);
        return p;
    }

    @Test
    void create_happyPath() {
        when(repository.existsByEmailAddress(validRequest.emailAddress())).thenReturn(false);
        when(repository.save(any(Professor.class))).thenAnswer(inv -> {
            Professor p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProfessorResponse resp = service.create(validRequest);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.salary()).isEqualByComparingTo("8500.00");
        assertThat(resp.status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void create_duplicateEmail() {
        when(repository.existsByEmailAddress(validRequest.emailAddress())).thenReturn(true);
        assertThatThrownBy(() -> service.create(validRequest))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void softDelete_marksDisabled() {
        Professor existing = persisted(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Professor.class))).thenAnswer(inv -> inv.getArgument(0));

        service.softDelete(1L);

        assertThat(existing.getStatus()).isEqualTo(Status.DISABLE);
    }

    @Test
    void restore_marksActive() {
        Professor existing = persisted(1L);
        existing.setStatus(Status.DISABLE);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Professor.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfessorResponse resp = service.restore(1L);

        assertThat(resp.status()).isEqualTo(Status.ACTIVE);
    }
}
