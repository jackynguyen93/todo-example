package com.example.todo.controllers;

import com.example.todo.models.Work;
import com.example.todo.services.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/works")
public class WorkController {

    @Autowired
    WorkService workService;

    @GetMapping
    public ResponseEntity<List<Work>> getAllWorks(
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "100") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection) {
        return ResponseEntity.ok(workService.getAllWorks(pageNo, pageSize, sortBy, sortDirection));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Work> getWork(@PathVariable long id) {
        Optional<Work> work = workService.getWork(id);
        return work.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWork(@PathVariable long id) {
        Optional<Work> work = workService.getWork(id);

        if (!work.isPresent()) return ResponseEntity.notFound().build();

        workService.deleteWork(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping()
    public ResponseEntity<Work> createWork(@Valid @RequestBody Work work) {
        Work savedWork = workService.createWork(work);
        return ResponseEntity.ok(savedWork);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Work> updateWork(@PathVariable long id, @Valid @RequestBody Work work) {
        Optional<Work> workOptional = workService.getWork(id);

        if (!workOptional.isPresent()) return ResponseEntity.notFound().build();

        Work updatedWork = workService.updateWork(id, work);
        return ResponseEntity.ok(updatedWork);
    }
}
