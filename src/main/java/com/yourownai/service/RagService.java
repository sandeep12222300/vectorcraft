package com.yourownai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourownai.dto.DocumentDtos;
import com.yourownai.dto.RagDtos;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * Provides RagService behavior.
 */
@Service
public class RagService {
    /**
     * Executes getLogger.
     */
    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    public static final String DOCUMENT_VERSION_KEY = "rag:documents:version";
    private final DocumentService documentService;
    private final OllamaService ollama;
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final double relevanceThreshold;
    private final Duration cacheTtl;

    public RagService(DocumentService documentService, OllamaService ollama, StringRedisTemplate redis, ObjectMapper mapper,
                      @Value("${vectordb.rag.relevance-threshold:0.35}") double relevanceThreshold,
                      @Value("${vectordb.rag.cache-ttl-seconds:3600}") long cacheTtlSeconds) {
        this.documentService = documentService;
        this.ollama = ollama;
        this.redis = redis;
        this.mapper = mapper;
        this.relevanceThreshold = relevanceThreshold;
        if (cacheTtlSeconds <= 0) {
            throw new IllegalArgumentException("RAG cache TTL must be positive");
        }
        this.cacheTtl = Duration.ofSeconds(cacheTtlSeconds);
    }

    /**
     * Executes ask.
     */
    public RagDtos.AskResponse ask(RagDtos.AskRequest req) {
        String key = "rag:v4:" + sha256(documentVersion() + "|" + req.question() + "|" + req.k());
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                return mapper.readValue(cached, RagDtos.AskResponse.class);
            }
        } catch (Exception e) {
            log.warn("RAG cache read failed; continuing without cache", e);
        }
        float[] queryEmbedding = ollama.embed(req.question());
        List<DocumentDtos.ChunkResult> contexts = documentService.search(queryEmbedding, req.k()).stream()
                .filter(context -> context.distance() <= relevanceThreshold)
                .collect(Collectors.toList());
        List<Float> embedding = new java.util.ArrayList<>(queryEmbedding.length);
        for (float value : queryEmbedding) embedding.add(value);
        if (contexts.isEmpty()) {
            RagDtos.AskResponse response = new RagDtos.AskResponse(
                    "No sufficiently relevant documents found.", ollama.generationModel(), embedding, List.of());
            try {
                redis.opsForValue().set(key, mapper.writeValueAsString(response), cacheTtl);
            } catch (Exception e) {
                log.warn("RAG cache write failed; returning uncached response", e);
            }
            return response;
        }
        StringBuilder prompt = new StringBuilder("You are a helpful assistant. Answer the question using the retrieved context when relevant. If the context is insufficient, clearly say so and avoid inventing citations.\n\nCONTEXT:\n");
        for (DocumentDtos.ChunkResult c : contexts)
            prompt.append("[Document: ").append(c.title()).append(", chunk ").append(c.chunkIndex()).append("]\n").append(c.content()).append("\n\n");
        prompt.append("QUESTION: ").append(req.question()).append("\nANSWER:");
        RagDtos.AskResponse response = new RagDtos.AskResponse(ollama.generate(prompt.toString()), ollama.generationModel(), embedding, contexts);
        try {
            redis.opsForValue().set(key, mapper.writeValueAsString(response), cacheTtl);
        } catch (Exception e) {
            log.warn("RAG cache write failed; returning uncached response", e);
        }
        return response;
    }

    /**
     * Executes documentVersion.
     */
    private String documentVersion() {
        try {
            return java.util.Optional.ofNullable(redis.opsForValue().get(DOCUMENT_VERSION_KEY)).orElse("0");
        } catch (Exception e) {
            log.warn("RAG document version read failed; using default version", e);
            return "0";
        }
    }

    /**
     * Executes sha256.
     */
    private String sha256(String value) {
        try {
            byte[] b = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            for (byte x : b) s.append(String.format("%02x", x));
            return s.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
