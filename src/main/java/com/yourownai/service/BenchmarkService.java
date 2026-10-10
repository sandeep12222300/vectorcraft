package com.yourownai.service;

import com.yourownai.model.*;
import com.yourownai.vectordb.VectorDatabase;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Provides BenchmarkService behavior.
 */
@Service
public class BenchmarkService {
    private final VectorDatabase db;

    /**
     * Executes BenchmarkService.
     */
    public BenchmarkService(VectorDatabase db) {
        this.db = db;
    }

    /**
     * Executes benchmark.
     */
    public Map<String, Object> benchmark() {
        List<VectorItem> items = db.all();
        if (items.isEmpty()) {
            return Map.of("message", "Insert vectors before benchmarking");
        }
        float[] q = items.get(0).values();
        Map<String, Object> out = new LinkedHashMap<>();
        for (SearchAlgorithm a : SearchAlgorithm.values()) {
            long start = System.nanoTime();
            var r = db.search(q, Math.min(5, items.size()), DistanceMetric.COSINE, a);
            long micros = (System.nanoTime() - start) / 1000;
            out.put(
                    a.name(),
                    Map.of(
                            "latencyMicros",
                            micros,
                            "results",
                            r.stream()
                                    .map(x -> Map.of("id", x.item().id(), "distance", x.distance()))
                                    .toList()));
        }
        return out;
    }
}
