package com.yourownai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provides DocumentChunker behavior.
 */
@Component
public class DocumentChunker {
    private final int chunkSize;
    private final int overlap;

    /**
     * Executes DocumentChunker.
     */
    public DocumentChunker(@Value("${vectordb.rag.chunk-size:250}") int chunkSize, @Value("${vectordb.rag.chunk-overlap:30}") int overlap) {
        if (chunkSize <= 0 || overlap < 0 || overlap >= chunkSize)
            throw new IllegalArgumentException("Invalid chunk configuration");
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    /**
     * Executes chunk.
     */
    public List<String> chunk(String text) {
        String[] words = text.trim().split("\\s+");
        if (words.length == 0 || text.isBlank()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        int start = 0;
        while (start < words.length) {
            int end = Math.min(words.length, start + chunkSize);
            out.add(String.join(" ", Arrays.copyOfRange(words, start, end)));
            if (end == words.length) break;
            start = end - overlap;
        }
        return out;
    }
}
