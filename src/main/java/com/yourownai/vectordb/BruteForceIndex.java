package com.yourownai.vectordb;

import com.yourownai.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides BruteForceIndex behavior.
 */
public class BruteForceIndex implements VectorIndex {
    private final Map<Long, VectorItem> items = new ConcurrentHashMap<>();

    /**
     * Executes add.
     */
    public void add(VectorItem item) {
        items.put(item.id(), item);
    }

    /**
     * Executes delete.
     */
    public void delete(long id) {
        items.remove(id);
    }

    /**
     * Executes size.
     */
    public int size() {
        return items.size();
    }

    /**
     * Executes search.
     */
    public List<ScoredVector> search(float[] query, int k, DistanceMetric metric) {
        return items.values().stream().map(v -> new ScoredVector(v, VectorMath.distance(query, v.values(), metric)))
                .sorted(Comparator.comparingDouble(ScoredVector::distance)).limit(k).toList();
    }

    /**
     * Executes items.
     */
    public Collection<VectorItem> items() {
        return items.values();
    }
}
