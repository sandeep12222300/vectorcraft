package com.yourownai.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Provides Document behavior.
 */
@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    /**
     * Executes now.
     */
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    /**
     * Executes Document.
     */
    protected Document() {
    }

    /**
     * Executes Document.
     */
    public Document(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * Executes getId.
     */
    public Long getId() {
        return id;
    }

    /**
     * Executes getTitle.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Executes getContent.
     */
    public String getContent() {
        return content;
    }

    /**
     * Executes getCreatedAt.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
