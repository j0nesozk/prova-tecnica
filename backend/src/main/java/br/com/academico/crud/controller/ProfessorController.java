package br.com.academico.crud.controller;

import br.com.academico.crud.dto.ProfessorRequest;
import br.com.academico.crud.dto.ProfessorResponse;
import br.com.academico.crud.service.ProfessorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/professors")
public class ProfessorController {

    private final ProfessorService service;

    public ProfessorController(ProfessorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProfessorResponse> create(@Valid @RequestBody ProfessorRequest req,
                                                    UriComponentsBuilder uriBuilder) {
        ProfessorResponse created = service.create(req);
        URI location = uriBuilder.path("/api/v1/professors/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<ProfessorResponse> list(Pageable pageable,
                                        @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return service.findAll(pageable, includeDisabled);
    }

    @GetMapping("/{id}")
    public ProfessorResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public ProfessorResponse update(@PathVariable Long id, @Valid @RequestBody ProfessorRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ProfessorResponse restore(@PathVariable Long id) {
        return service.restore(id);
    }
}
