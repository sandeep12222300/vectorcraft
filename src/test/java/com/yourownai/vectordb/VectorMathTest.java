package com.yourownai.vectordb;

import com.yourownai.model.DistanceMetric;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Provides VectorMathTest behavior.
 */
class VectorMathTest {
    /**
     * Executes euclidean.
     */
    @Test
    void euclidean() {
        assertEquals(5.0, VectorMath.distance(new float[]{0, 0}, new float[]{3, 4}, DistanceMetric.EUCLIDEAN), 1e-6);
    }

    /**
     * Executes manhattan.
     */
    @Test
    void manhattan() {
        assertEquals(7.0, VectorMath.distance(new float[]{1, 2}, new float[]{4, 6}, DistanceMetric.MANHATTAN), 1e-6);
    }

    /**
     * Executes cosine.
     */
    @Test
    void cosine() {
        assertEquals(0.0, VectorMath.distance(new float[]{1, 0}, new float[]{2, 0}, DistanceMetric.COSINE), 1e-6);
    }
}
