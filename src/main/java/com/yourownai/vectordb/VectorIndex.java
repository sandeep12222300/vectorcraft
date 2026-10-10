package com.yourownai.vectordb;

import com.yourownai.model.*;

import java.util.List;

/**
 * Defines the contract for VectorIndex.
 */
public interface VectorIndex {
    /**
     * Executes add.
     */
    void add(VectorItem item);

    /**
     * Executes delete.
     */
    void delete(long id);

    /**
     * Executes search.
     */
    List<ScoredVector> search(float[] query, int k, DistanceMetric metric);

    /**
     * Executes size.
     */
    int size();

    /**
     * Represents ScoredVector data.
     */
    record ScoredVector(VectorItem item, double distance) {
    }
}
