package com.yourownai.model;

import java.util.Map;

/**
 * Represents VectorItem data.
 */
public record VectorItem(long id, String label, String category, float[] values, Map<String, String> metadata) {
    public VectorItem {
        values = values.clone();
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
