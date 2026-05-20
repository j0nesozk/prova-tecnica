package br.com.academico.crud.service;

import br.com.academico.crud.domain.entity.Address;
import br.com.academico.crud.domain.entity.Person;
import br.com.academico.crud.dto.AddressRequest;
import br.com.academico.crud.dto.AddressResponse;
import br.com.academico.crud.exception.ResourceNotFoundException;
import br.com.academico.crud.repository.AddressRepository;
import br.com.academico.crud.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final PersonRepository personRepository;

    public AddressService(AddressRepository addressRepository, PersonRepository personRepository) {
        this.addressRepository = addressRepository;
        this.personRepository = personRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> findAllByPerson(Long personId) {
        ensurePersonExists(personId);
        return addressRepository.findAllByPersonId(personId).stream()
                .map(AddressResponse::from)
                .toList();
    }

    public AddressResponse create(Long personId, AddressRequest req) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> ResourceNotFoundException.of("Person", personId));
        Address addr = req.toEntity();
        addr.setPerson(person);
        return AddressResponse.from(addressRepository.save(addr));
    }

    public AddressResponse update(Long personId, Long addressId, AddressRequest req) {
        Address existing = addressRepository.findByIdAndPersonId(addressId, personId)
                .orElseThrow(() -> ResourceNotFoundException.of("Address", addressId));
        req.applyTo(existing);
        return AddressResponse.from(addressRepository.save(existing));
    }

    public void delete(Long personId, Long addressId) {
        Address existing = addressRepository.findByIdAndPersonId(addressId, personId)
                .orElseThrow(() -> ResourceNotFoundException.of("Address", addressId));
        Person owner = existing.getPerson();
        owner.removeAddress(existing);
        personRepository.save(owner);
    }

    private void ensurePersonExists(Long personId) {
        if (!personRepository.existsById(personId)) {
            throw ResourceNotFoundException.of("Person", personId);
        }
    }
}
