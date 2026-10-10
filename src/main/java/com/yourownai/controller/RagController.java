package com.yourownai.controller;

import com.yourownai.dto.RagDtos;
import com.yourownai.service.RagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Provides RagController behavior.
 */
@RestController
@RequestMapping("/api/v1/rag")
public class RagController {
    private final RagService service;

    /**
     * Executes RagController.
     */
    public RagController(RagService service) {
        this.service = service;
    }

    /**
     * Executes ask.
     */
    @PostMapping("/ask")
    public RagDtos.AskResponse ask(@Valid @RequestBody RagDtos.AskRequest request) {
        return service.ask(request);
    }
}
