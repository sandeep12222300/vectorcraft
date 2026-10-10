package com.yourownai.vectordb;

import com.yourownai.model.DistanceMetric;

/**
 * Provides VectorMath behavior.
 */
public final class VectorMath {
    /**
     * Executes VectorMath.
     */
    private VectorMath() {
    }

    /**
     * Executes distance.
     */
    public static double distance(float[] a, float[] b, DistanceMetric metric) {
        if (a.length != b.length)
            throw new IllegalArgumentException("Vector dimensions differ: " + a.length + " vs " + b.length);
        return switch (metric) {
            case EUCLIDEAN -> euclidean(a, b);
            case MANHATTAN -> manhattan(a, b);
            case COSINE -> cosineDistance(a, b);
        };
    }

    /**
     * Executes euclidean.
     */
    public static double euclidean(float[] a, float[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    /**
     * Executes manhattan.
     */
    public static double manhattan(float[] a, float[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.abs(a[i] - b[i]);
        }
        return sum;
    }

    /**
     * Executes cosineDistance.
     */
    public static double cosineDistance(float[] a, float[] b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0) {
            return 1.0;
        }
        return 1.0 - dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
