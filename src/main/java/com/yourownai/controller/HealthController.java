package com.yourownai.controller;

import com.yourownai.service.DocumentVectorStore;
import com.yourownai.vectordb.VectorDatabase;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Provides HealthController behavior.
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {
    private final VectorDatabase vectors;
    private final DocumentVectorStore documents;

    /**
     * Executes HealthController.
     */
    public HealthController(VectorDatabase vectors, DocumentVectorStore documents) {
        this.vectors = vectors;
        this.documents = documents;
    }

    /**
     * Executes status.
     */
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("status", "UP", "demoVectors", vectors.size(), "demoDimension", vectors.dimension(), "documentChunks", documents.size());
    }
}
