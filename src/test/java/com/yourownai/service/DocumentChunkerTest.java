package com.yourownai.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Provides DocumentChunkerTest behavior.
 */
class DocumentChunkerTest {
    /**
     * Executes chunksWithOverlap.
     */
    @Test
    void chunksWithOverlap() {
        DocumentChunker c = new DocumentChunker(5, 1);
        var out = c.chunk("one two three four five six seven eight nine");
        assertEquals(2, out.size());
        assertTrue(out.get(0).contains("five"));
        assertTrue(out.get(1).startsWith("five"));
    }
}
