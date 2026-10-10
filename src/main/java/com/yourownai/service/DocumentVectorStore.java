package com.yourownai.service;

import com.yourownai.model.*;
import com.yourownai.repository.DocumentChunkRepository;
import com.yourownai.vectordb.HnswIndex;
import com.yourownai.vectordb.VectorIndex;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides DocumentVectorStore behavior.
 */
@Component
public class DocumentVectorStore {
    private final DocumentChunkRepository repository;
    private final ObjectMapper mapper;
    private final HnswIndex index;
    private final Map<Long, DocumentChunk> chunks = new ConcurrentHashMap<>();

    /**
     * Executes DocumentVectorStore.
     */
    public DocumentVectorStore(DocumentChunkRepository repository, ObjectMapper mapper, @Value("${vectordb.hnsw.m:16}") int m, @Value("${vectordb.hnsw.ef-construction:100}") int ef) {
        this.repository = repository;
        this.mapper = mapper;
        this.index = new HnswIndex(m, ef);
    }

    /**
     * Executes rebuild.
     */
    @PostConstruct
    public void rebuild() {
        repository.findAllByOrderByIdAsc().forEach(c -> {
            try {
                chunks.put(c.getId(), c);
                index.add(toVector(c));
            } catch (Exception e) {
                throw new IllegalStateException("Cannot rebuild vector index for chunk " + c.getId(), e);
            }
        });
    }

    /**
     * Executes add.
     */
    public void add(DocumentChunk c) {
        chunks.put(c.getId(), c);
        index.add(toVector(c));
    }

    /**
     * Executes search.
     */
    public List<VectorIndex.ScoredVector> search(float[] query, int k) {
        return index.search(query, k, DistanceMetric.COSINE);
    }

    /**
     * Executes all.
     */
    public List<VectorItem> all() {
        return chunks.values().stream().map(this::toVector).toList();
    }

    /**
     * Executes toVector.
     */
    private VectorItem toVector(DocumentChunk c) {
        try {
            List<Float> values = mapper.readValue(c.getEmbeddingJson(), new TypeReference<>() {
            });
            float[] a = new float[values.size()];
            for (int i = 0; i < a.length; i++) {
                a[i] = values.get(i);
            }
            return new VectorItem(c.getId(), String.valueOf(c.getDocumentId()), "document", a, Map.of("chunkIndex", String.valueOf(c.getChunkIndex())));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Executes delete.
     */
    public void delete(long id) {
        chunks.remove(id);
        index.delete(id);
    }

    /**
     * Executes get.
     */
    public DocumentChunk get(long id) {
        return chunks.get(id);
    }

    /**
     * Executes size.
     */
    public int size() {
        return chunks.size();
    }
}
