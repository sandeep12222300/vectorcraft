package com.yourownai.repository;

import com.yourownai.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Defines the contract for DocumentChunkRepository.
 */
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    /**
     * Executes findAllByOrderByIdAsc.
     */
    List<DocumentChunk> findAllByOrderByIdAsc();

    /**
     * Executes findByDocumentIdOrderByChunkIndexAsc.
     */
    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(Long documentId);
}
