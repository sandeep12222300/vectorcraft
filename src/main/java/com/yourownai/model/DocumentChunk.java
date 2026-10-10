package com.yourownai.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Provides DocumentChunk behavior.
 */
@Entity
@Table(name = "document_chunks", indexes = @Index(name = "idx_chunk_document", columnList = "documentId"))
public class DocumentChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long documentId;
    @Column(nullable = false)
    private int chunkIndex;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String embeddingJson;
    @Column(nullable = false)
    private int dimension;
    /**
     * Executes now.
     */
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    /**
     * Executes DocumentChunk.
     */
    protected DocumentChunk() {
    }

    /**
     * Executes DocumentChunk.
     */
    public DocumentChunk(Long documentId, int chunkIndex, String content, String embeddingJson, int dimension) {
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.embeddingJson = embeddingJson;
        this.dimension = dimension;
    }

    /**
     * Executes getId.
     */
    public Long getId() {
        return id;
    }

    /**
     * Executes getDocumentId.
     */
    public Long getDocumentId() {
        return documentId;
    }

    /**
     * Executes getChunkIndex.
     */
    public int getChunkIndex() {
        return chunkIndex;
    }

    /**
     * Executes getContent.
     */
    public String getContent() {
        return content;
    }

    /**
     * Executes getEmbeddingJson.
     */
    public String getEmbeddingJson() {
        return embeddingJson;
    }

    /**
     * Executes getDimension.
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Executes getCreatedAt.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
