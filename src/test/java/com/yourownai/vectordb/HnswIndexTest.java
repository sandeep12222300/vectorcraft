package com.yourownai.vectordb;

import com.yourownai.model.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Provides HnswIndexTest behavior.
 */
class HnswIndexTest {
    /**
     * Executes returnsNearestNeighborsWithHighRecall.
     */
    @Test
    void returnsNearestNeighborsWithHighRecall() {
        BruteForceIndex exact = new BruteForceIndex();
        HnswIndex hnsw = new HnswIndex(8, 80);
        for (int i = 0; i < 200; i++) {
            float[] v = {(float) i, (float) (i % 11), (float) (i % 5)};
            VectorItem item = new VectorItem(i, "v" + i, "test", v, Map.of());
            exact.add(item);
            hnsw.add(item);
        }
        float[] q = {117.4f, 5f, 2f};
        var truth = exact.search(q, 10, DistanceMetric.EUCLIDEAN);
        var approx = hnsw.search(q, 10, DistanceMetric.EUCLIDEAN);
        Set<Long> ids = truth.stream().map(x -> x.item().id()).collect(Collectors.toSet());
        long hits = approx.stream().filter(x -> ids.contains(x.item().id())).count();
        assertTrue(hits >= 7, "Expected recall >= 0.7 but got " + hits / 10.0);
    }

    /**
     * Executes remainsSearchableAfterDeletingEntryPoint.
     */
    @Test
    void remainsSearchableAfterDeletingEntryPoint() {
        HnswIndex hnsw = new HnswIndex(8, 80);
        for (int i = 0; i < 100; i++)
            hnsw.add(new VectorItem(i, "v" + i, "test", new float[]{i, (float) (i % 7)}, Map.of()));
        hnsw.delete(0);
        var results = hnsw.search(new float[]{50.1f, 1f}, 5, DistanceMetric.COSINE);
        assertEquals(5, results.size());
        assertTrue(results.stream().noneMatch(result -> result.item().id() == 0));
    }
}
