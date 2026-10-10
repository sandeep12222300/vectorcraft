package com.yourownai.dto;

import jakarta.validation.constraints.*;

import java.util.List;

/**
 * Provides DocumentDtos behavior.
 */
public final class DocumentDtos {
    /**
     * Executes DocumentDtos.
     */
    private DocumentDtos() {
    }

    /**
     * Represents InsertRequest data.
     */
    public record InsertRequest(@NotBlank String title, @NotBlank String text) {
    }

    /**
     * Represents SearchRequest data.
     */
    public record SearchRequest(@NotBlank String question, @Min(1) @Max(20) int k) {
    }

    /**
     * Represents DocumentResponse data.
     */
    public record DocumentResponse(long id, String title, int chunks) {
    }

    /**
     * Represents ChunkResult data.
     */
    public record ChunkResult(long vectorId, long chunkId, long documentId, String title, int chunkIndex,
                              String content, double distance) {
        /**
         * Executes ChunkResult.
         */
        public ChunkResult(long chunkId, long documentId, String title, int chunkIndex, String content, double distance) {
            this(chunkId, chunkId, documentId, title, chunkIndex, content, distance);
        }
    }
}
