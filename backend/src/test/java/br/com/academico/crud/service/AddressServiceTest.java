package br.com.academico.crud.service;

import br.com.academico.crud.domain.entity.Address;
import br.com.academico.crud.domain.entity.Person;
import br.com.academico.crud.domain.entity.Student;
import br.com.academico.crud.dto.AddressRequest;
import br.com.academico.crud.dto.AddressResponse;
import br.com.academico.crud.exception.ResourceNotFoundException;
import br.com.academico.crud.repository.AddressRepository;
import br.com.academico.crud.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    AddressRepository addressRepository;

    @Mock
    PersonRepository personRepository;

    @InjectMocks
    AddressService service;

    Student owner;
    AddressRequest validRequest;

    @BeforeEach
    void setUp() {
        owner = new Student("Maria", "+5511999998888", "maria@example.com", "STU-001", null);
        owner.setId(1L);
        validRequest = new AddressRequest("Rua A", "São Paulo", "SP", "01000-000", "Brasil");
    }

    @Test
    void findAllByPerson_returnsList() {
        when(personRepository.existsById(1L)).thenReturn(true);
        Address a = validRequest.toEntity();
        a.setId(10L);
        when(addressRepository.findAllByPersonId(1L)).thenReturn(List.of(a));

        List<AddressResponse> result = service.findAllByPerson(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).city()).isEqualTo("São Paulo");
    }

    @Test
    void findAllByPerson_personMissing() {
        when(personRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> service.findAllByPerson(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_attachesAddressToPerson() {
        when(personRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(personRepository.save(any(Person.class))).thenAnswer(inv -> {
            Person p = inv.getArgument(0);
            p.getAddresses().forEach(a -> { if (a.getId() == null) a.setId(20L); });
            return p;
        });

        AddressResponse resp = service.create(1L, validRequest);

        assertThat(resp.id()).isEqualTo(20L);
        assertThat(owner.getAddresses()).hasSize(1);
        assertThat(owner.getAddresses().get(0).getPerson()).isSameAs(owner);
    }

    @Test
    void update_changesExistingAddress() {
        Address existing = validRequest.toEntity();
        existing.setId(5L);
        existing.setPerson(owner);
        when(addressRepository.findByIdAndPersonId(5L, 1L)).thenReturn(Optional.of(existing));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressRequest changed = new AddressRequest("Rua B", "Rio", "RJ", "20000-000", "Brasil");
        AddressResponse resp = service.update(1L, 5L, changed);

        assertThat(resp.street()).isEqualTo("Rua B");
        assertThat(resp.city()).isEqualTo("Rio");
    }

    @Test
    void delete_removesAddressFromPerson() {
        Address existing = validRequest.toEntity();
        existing.setId(5L);
        owner.addAddress(existing);
        when(addressRepository.findByIdAndPersonId(5L, 1L)).thenReturn(Optional.of(existing));

        service.delete(1L, 5L);

        assertThat(owner.getAddresses()).isEmpty();
    }

    @Test
    void delete_notFound() {
        when(addressRepository.findByIdAndPersonId(5L, 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(1L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
