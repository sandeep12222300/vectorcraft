package com.yourownai.dto;

import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides RagDtos behavior.
 */
public final class RagDtos {
    /**
     * Executes RagDtos.
     */
    private RagDtos() {
    }

    /**
     * Represents AskRequest data.
     */
    public record AskRequest(@NotBlank String question, @Min(1) @Max(10) int k) {
    }

    /**
     * Represents AskResponse data.
     */
    public record AskResponse(String answer, String model, List<Float> queryEmbedding,
                              List<DocumentDtos.ChunkResult> contexts) {
    }
}
