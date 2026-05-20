package br.com.academico.crud.controller;

import br.com.academico.crud.dto.StudentRequest;
import br.com.academico.crud.dto.StudentResponse;
import br.com.academico.crud.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest req,
                                                  UriComponentsBuilder uriBuilder) {
        StudentResponse created = service.create(req);
        URI location = uriBuilder.path("/api/v1/students/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public Page<StudentResponse> list(Pageable pageable,
                                      @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return service.findAll(pageable, includeDisabled);
    }

    @GetMapping("/{id}")
    public StudentResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public StudentResponse restore(@PathVariable Long id) {
        return service.restore(id);
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StudentResponse uploadPhoto(@PathVariable Long id,
                                       @RequestPart("file") MultipartFile file) {
        return service.uploadPhoto(id, file);
    }

    @DeleteMapping("/{id}/photo")
    public StudentResponse deletePhoto(@PathVariable Long id) {
        return service.deletePhoto(id);
    }
}
