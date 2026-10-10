package com.yourownai.controller;

import com.yourownai.dto.DocumentDtos;
import com.yourownai.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Provides DocumentController behavior.
 */
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {
    private final DocumentService service;

    /**
     * Executes DocumentController.
     */
    public DocumentController(DocumentService service) {
        this.service = service;
    }

    /**
     * Executes insert.
     */
    @PostMapping
    public DocumentDtos.DocumentResponse insert(@Valid @RequestBody DocumentDtos.InsertRequest request) {
        return service.insert(request);
    }

    /**
     * Executes search.
     */
    @PostMapping("/search")
    public List<DocumentDtos.ChunkResult> search(@Valid @RequestBody DocumentDtos.SearchRequest request) {
        return service.search(request.question(), request.k());
    }

    /**
     * Executes list.
     */
    @GetMapping
    public List<DocumentDtos.DocumentResponse> list() {
        return service.list();
    }

    /**
     * Executes vectors.
     */
    @GetMapping("/vectors")
    public List<com.yourownai.dto.VectorDtos.Response> vectors() {
        return service.vectors().stream().map(v -> new com.yourownai.dto.VectorDtos.Response(
                -v.id(), v.label(), v.category(), toValues(v.values()), v.metadata())).toList();
    }

    /**
     * Executes delete.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }

    /**
     * Executes toValues.
     */
    private static List<Float> toValues(float[] values) {
        List<Float> result = new ArrayList<>(values.length);
        for (float value : values) result.add(value);
        return result;
    }
}
