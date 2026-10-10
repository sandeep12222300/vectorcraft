package com.yourownai.dto;

import com.yourownai.model.*;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Map;

/**
 * Provides VectorDtos behavior.
 */
public final class VectorDtos {
    /**
     * Executes VectorDtos.
     */
    private VectorDtos() {
    }

    /**
     * Represents InsertRequest data.
     */
    public record InsertRequest(@NotBlank String label, @NotBlank String category,
                                @NotEmpty List<@NotNull Float> values, Map<String, String> metadata) {
    }

    /**
     * Represents SearchRequest data.
     */
    public record SearchRequest(@NotEmpty List<@NotNull Float> query, @Min(1) @Max(100) int k, DistanceMetric metric,
                                SearchAlgorithm algorithm) {
    }

    /**
     * Represents Response data.
     */
    public record Response(long id, String label, String category, List<Float> values, Map<String, String> metadata) {
    }

    /**
     * Represents SearchResult data.
     */
    public record SearchResult(long id, String label, String category, double distance, Map<String, String> metadata) {
    }
}
