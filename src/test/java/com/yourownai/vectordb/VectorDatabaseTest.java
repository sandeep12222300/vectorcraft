package com.yourownai.vectordb;

import com.yourownai.model.DistanceMetric;
import com.yourownai.model.SearchAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Provides VectorDatabaseTest behavior.
 */
class VectorDatabaseTest {
    /**
     * Executes rejectsInvalidValuesAndMismatchedQueries.
     */
    @Test
    void rejectsInvalidValuesAndMismatchedQueries() {
        VectorDatabase db = new VectorDatabase(8, 40);
        assertThrows(IllegalArgumentException.class,
                () -> db.add("bad", "test", new float[]{Float.NaN}, Map.of()));

        db.add("one", "test", new float[]{1, 0}, Map.of());
        assertThrows(IllegalArgumentException.class,
                () -> db.search(new float[]{1}, 1, DistanceMetric.COSINE, SearchAlgorithm.BRUTE_FORCE));
    }

    /**
     * Executes nonCosineHnswSearchMatchesExactOrdering.
     */
    @Test
    void nonCosineHnswSearchMatchesExactOrdering() {
        VectorDatabase db = new VectorDatabase(8, 40);
        db.add("near", "test", new float[]{3, 4}, Map.of());
        db.add("far", "test", new float[]{10, 10}, Map.of());

        List<VectorIndex.ScoredVector> results =
                db.search(new float[]{0, 0}, 2, DistanceMetric.EUCLIDEAN, SearchAlgorithm.HNSW);

        assertEquals("near", results.get(0).item().label());
        assertEquals(5.0, results.get(0).distance(), 1e-6);
    }

    /**
     * Executes deletionRemovesVectorFromEveryIndex.
     */
    @Test
    void deletionRemovesVectorFromEveryIndex() {
        VectorDatabase db = new VectorDatabase(8, 40);
        long id = db.add("one", "test", new float[]{1, 0}, Map.of()).id();
        db.add("two", "test", new float[]{0, 1}, Map.of());

        db.delete(id);

        assertEquals(1, db.size());
        assertTrue(db.search(new float[]{1, 0}, 2, DistanceMetric.COSINE, SearchAlgorithm.BRUTE_FORCE)
                .stream().noneMatch(result -> result.item().id() == id));
        assertTrue(db.search(new float[]{1, 0}, 2, DistanceMetric.COSINE, SearchAlgorithm.HNSW)
                .stream().noneMatch(result -> result.item().id() == id));
    }
}
