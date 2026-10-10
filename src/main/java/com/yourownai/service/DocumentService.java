package com.yourownai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourownai.dto.DocumentDtos;
import com.yourownai.exception.ResourceNotFoundException;
import com.yourownai.model.Document;
import com.yourownai.model.DocumentChunk;
import com.yourownai.repository.DocumentChunkRepository;
import com.yourownai.repository.DocumentRepository;
import com.yourownai.vectordb.VectorIndex;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

/**
 * Provides DocumentService behavior.
 */
@Service
public class DocumentService {
    /**
     * Executes getLogger.
     */
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private final DocumentRepository documents;
    private final DocumentChunkRepository chunks;
    private final DocumentChunker chunker;
    private final OllamaService ollama;
    private final DocumentVectorStore store;
    private final ObjectMapper mapper;
    private final StringRedisTemplate redis;

    /**
     * Executes DocumentService.
     */
    public DocumentService(DocumentRepository documents, DocumentChunkRepository chunks, DocumentChunker chunker, OllamaService ollama, DocumentVectorStore store, ObjectMapper mapper, StringRedisTemplate redis) {
        this.documents = documents;
        this.chunks = chunks;
        this.chunker = chunker;
        this.ollama = ollama;
        this.store = store;
        this.mapper = mapper;
        this.redis = redis;
    }

    /**
     * Executes insert.
     */
    @Transactional
    public DocumentDtos.DocumentResponse insert(DocumentDtos.InsertRequest req) {
        Document doc = documents.save(new Document(req.title(), req.text()));
        List<String> pieces = chunker.chunk(req.text());
        List<DocumentChunk> persisted = new ArrayList<>();
        int i = 0;
        for (String text : pieces) {
            float[] embedding = ollama.embed(text);
            try {
                List<Float> vals = new ArrayList<>(embedding.length);
                for (float x : embedding) vals.add(x);
                persisted.add(chunks.save(new DocumentChunk(doc.getId(), i++, text, mapper.writeValueAsString(vals), embedding.length)));
            } catch (Exception e) {
                throw new IllegalStateException("Could not persist document chunk", e);
            }
        }
        afterCommit(() -> {
            persisted.forEach(store::add);
            invalidateRagCache();
        });
        return new DocumentDtos.DocumentResponse(doc.getId(), doc.getTitle(), pieces.size());
    }

    /**
     * Executes search.
     */
    public List<DocumentDtos.ChunkResult> search(String question, int k) {
        return search(ollama.embed(question), k);
    }

    /**
     * Executes search.
     */
    public List<DocumentDtos.ChunkResult> search(float[] queryEmbedding, int k) {
        return store.search(queryEmbedding, k).stream().map(this::result).toList();
    }

    /**
     * Executes vectors.
     */
    public List<com.yourownai.model.VectorItem> vectors() {
        return store.all();
    }

    /**
     * Executes result.
     */
    private DocumentDtos.ChunkResult result(VectorIndex.ScoredVector s) {
        DocumentChunk c = store.get(s.item().id());
        String title = documents.findById(c.getDocumentId()).map(Document::getTitle).orElse("unknown");
        return new DocumentDtos.ChunkResult(s.item().id(), c.getId(), c.getDocumentId(), title, c.getChunkIndex(), c.getContent(), s.distance());
    }

    /**
     * Executes delete.
     */
    @Transactional
    public void delete(long id) {
        if (!documents.existsById(id)) {
            throw new ResourceNotFoundException("Document not found: " + id);
        }
        List<DocumentChunk> cs = chunks.findByDocumentIdOrderByChunkIndexAsc(id);
        chunks.deleteAll(cs);
        documents.deleteById(id);
        afterCommit(() -> {
            cs.forEach(c -> store.delete(c.getId()));
            invalidateRagCache();
        });
    }

    /**
     * Executes list.
     */
    public List<DocumentDtos.DocumentResponse> list() {
        return documents.findAll().stream().map(d -> new DocumentDtos.DocumentResponse(d.getId(), d.getTitle(), chunks.findByDocumentIdOrderByChunkIndexAsc(d.getId()).size())).toList();
    }

    /**
     * Executes afterCommit.
     */
    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                /** Executes afterCommit. */
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    /**
     * Executes invalidateRagCache.
     */
    private void invalidateRagCache() {
        try {
            redis.opsForValue().increment(RagService.DOCUMENT_VERSION_KEY);
        } catch (Exception e) {
            log.warn("Could not invalidate RAG cache; entries will expire by TTL", e);
        }
    }
}
