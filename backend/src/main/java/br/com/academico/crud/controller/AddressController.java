package br.com.academico.crud.controller;

import br.com.academico.crud.dto.AddressRequest;
import br.com.academico.crud.dto.AddressResponse;
import br.com.academico.crud.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/persons/{personId}/addresses")
public class AddressController {

    private final AddressService service;

    public AddressController(AddressService service) {
        this.service = service;
    }

    @GetMapping
    public List<AddressResponse> list(@PathVariable Long personId) {
        return service.findAllByPerson(personId);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(@PathVariable Long personId,
                                                  @Valid @RequestBody AddressRequest req,
                                                  UriComponentsBuilder uriBuilder) {
        AddressResponse created = service.create(personId, req);
        URI location = uriBuilder.path("/api/v1/persons/{personId}/addresses/{id}")
                .buildAndExpand(personId, created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{addressId}")
    public AddressResponse update(@PathVariable Long personId,
                                  @PathVariable Long addressId,
                                  @Valid @RequestBody AddressRequest req) {
        return service.update(personId, addressId, req);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> delete(@PathVariable Long personId, @PathVariable Long addressId) {
        service.delete(personId, addressId);
        return ResponseEntity.noContent().build();
    }
}
