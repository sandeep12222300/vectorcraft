package com.yourownai.service;

import com.yourownai.dto.VectorDtos;
import com.yourownai.model.*;
import com.yourownai.vectordb.VectorDatabase;
import com.yourownai.vectordb.VectorIndex;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Provides VectorService behavior.
 */
@Service
public class VectorService {
    private final VectorDatabase db;

    /**
     * Executes VectorService.
     */
    public VectorService(VectorDatabase db) {
        this.db = db;
    }

    /**
     * Executes insert.
     */
    public VectorDtos.Response insert(VectorDtos.InsertRequest r) {
        VectorItem v = db.add(r.label(), r.category(), toArray(r.values()), r.metadata());
        return response(v);
    }

    /**
     * Executes all.
     */
    public List<VectorDtos.Response> all() {
        return db.all().stream().map(this::response).toList();
    }

    /**
     * Executes delete.
     */
    public void delete(long id) {
        db.delete(id);
    }

    /**
     * Executes search.
     */
    public List<VectorDtos.SearchResult> search(VectorDtos.SearchRequest r) {
        DistanceMetric metric = r.metric() == null ? DistanceMetric.COSINE : r.metric();
        SearchAlgorithm algo = r.algorithm() == null ? SearchAlgorithm.HNSW : r.algorithm();
        return db.search(toArray(r.query()), r.k(), metric, algo).stream().map(x -> new VectorDtos.SearchResult(x.item().id(), x.item().label(), x.item().category(), x.distance(), x.item().metadata())).toList();
    }

    /**
     * Executes stats.
     */
    public Map<String, Object> stats() {
        return Map.of("count", db.size(), "dimension", db.dimension());
    }

    /**
     * Executes response.
     */
    private VectorDtos.Response response(VectorItem v) {
        List<Float> values = new ArrayList<>();
        for (float x : v.values()) values.add(x);
        return new VectorDtos.Response(v.id(), v.label(), v.category(), values, v.metadata());
    }

    /**
     * Executes toArray.
     */
    private float[] toArray(List<Float> v) {
        float[] a = new float[v.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = v.get(i);
        }
        return a;
    }
}
