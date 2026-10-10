package com.yourownai.repository;

import com.yourownai.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Defines the contract for DocumentRepository.
 */
public interface DocumentRepository extends JpaRepository<Document, Long> {
}
