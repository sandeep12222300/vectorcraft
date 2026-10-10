package com.yourownai.controller;

import com.yourownai.dto.VectorDtos;
import com.yourownai.service.BenchmarkService;
import com.yourownai.service.VectorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Provides VectorController behavior.
 */
@RestController
@RequestMapping("/api/v1/vectors")
public class VectorController {
    private final VectorService service;
    private final BenchmarkService benchmark;

    /**
     * Executes VectorController.
     */
    public VectorController(VectorService service, BenchmarkService benchmark) {
        this.service = service;
        this.benchmark = benchmark;
    }

    /**
     * Executes insert.
     */
    @PostMapping
    public VectorDtos.Response insert(@Valid @RequestBody VectorDtos.InsertRequest request) {
        return service.insert(request);
    }

    /**
     * Executes all.
     */
    @GetMapping
    public List<VectorDtos.Response> all() {
        return service.all();
    }

    /**
     * Executes stats.
     */
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return service.stats();
    }

    /**
     * Executes search.
     */
    @PostMapping("/search")
    public List<VectorDtos.SearchResult> search(@Valid @RequestBody VectorDtos.SearchRequest request) {
        return service.search(request);
    }

    /**
     * Executes delete.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Executes benchmark.
     */
    @GetMapping("/benchmark")
    public Map<String, Object> benchmark() {
        return benchmark.benchmark();
    }
}
