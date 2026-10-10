package com.yourownai.vectordb;

import com.yourownai.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Provides VectorDatabase behavior.
 */
@Component
public class VectorDatabase {
    /**
     * Executes BruteForceIndex.
     */
    private final BruteForceIndex bruteForce = new BruteForceIndex();
    /**
     * Executes KdTreeIndex.
     */
    private final KdTreeIndex kdTree = new KdTreeIndex();
    private final HnswIndex hnsw;
    private final Map<Long, VectorItem> items = new HashMap<>();
    /**
     * Executes AtomicLong.
     */
    private final AtomicLong ids = new AtomicLong();

    /**
     * Executes VectorDatabase.
     */
    public VectorDatabase(@Value("${vectordb.hnsw.m:16}") int m, @Value("${vectordb.hnsw.ef-construction:100}") int ef) {
        hnsw = new HnswIndex(m, ef);
    }

    /**
     * Executes add.
     */
    public synchronized VectorItem add(String label, String category, float[] values, Map<String, String> metadata) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Vector must not be empty");
        }
        validateFinite(values);
        if (!items.isEmpty() && values.length != items.values().iterator().next().values().length)
            throw new IllegalArgumentException("All vectors must have the same dimension");
        VectorItem item = new VectorItem(ids.incrementAndGet(), label, category, values, metadata);
        items.put(item.id(), item);
        bruteForce.add(item);
        kdTree.add(item);
        hnsw.add(item);
        return item;
    }

    /**
     * Executes delete.
     */
    public synchronized void delete(long id) {
        if (items.remove(id) != null) {
            bruteForce.delete(id);
            kdTree.delete(id);
            hnsw.delete(id);
        }
    }

    /**
     * Executes search.
     */
    public synchronized List<VectorIndex.ScoredVector> search(float[] q, int k, DistanceMetric metric, SearchAlgorithm algorithm) {
        if (q == null || q.length == 0) {
            throw new IllegalArgumentException("Query vector must not be empty");
        }
        validateFinite(q);
        if (k < 1) {
            throw new IllegalArgumentException("k must be >= 1");
        }
        if (metric == null || algorithm == null) {
            throw new IllegalArgumentException("Metric and algorithm are required");
        }
        if (!items.isEmpty() && q.length != dimension())
            throw new IllegalArgumentException("Query dimension does not match stored vectors");
        return switch (algorithm) {
            case BRUTE_FORCE -> bruteForce.search(q, k, metric);
            case KD_TREE -> kdTree.search(q, k, metric);
            case HNSW -> hnsw.search(q, k, metric);
        };
    }

    /**
     * Executes all.
     */
    public synchronized List<VectorItem> all() {
        return List.copyOf(items.values());
    }

    /**
     * Executes size.
     */
    public synchronized int size() {
        return items.size();
    }

    /**
     * Executes dimension.
     */
    public synchronized int dimension() {
        return items.values().stream().findFirst().map(v -> v.values().length).orElse(0);
    }

    /**
     * Executes validateFinite.
     */
    private static void validateFinite(float[] values) {
        for (float value : values)
            if (!Float.isFinite(value)) {
                throw new IllegalArgumentException("Vector values must be finite");
            }
    }
}
