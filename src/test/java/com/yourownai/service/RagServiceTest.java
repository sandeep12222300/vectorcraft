package com.yourownai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourownai.dto.DocumentDtos;
import com.yourownai.dto.RagDtos;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Provides RagServiceTest behavior.
 */
class RagServiceTest {
    /**
     * Executes usesTheReturnedEmbeddingForVectorSearchAndResponse.
     */
    @Test
    void usesTheReturnedEmbeddingForVectorSearchAndResponse() {
        DocumentService documents = mock(DocumentService.class);
        OllamaService ollama = mock(OllamaService.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenReturn(null);

        float[] embedding = {0.1f, 0.2f, 0.3f};
        when(ollama.embed("What is Spring Boot used for?")).thenReturn(embedding);
        when(ollama.generate(anyString())).thenReturn("It is used to build Spring applications.");
        when(ollama.generationModel()).thenReturn("test-model");
        DocumentDtos.ChunkResult context = new DocumentDtos.ChunkResult(42L, 42L, 7L, "Spring", 0, "Spring context", 0.1234);
        when(documents.search(same(embedding), eq(3))).thenReturn(java.util.List.of(context));

        RagDtos.AskResponse response = new RagService(documents, ollama, redis, new ObjectMapper(), 0.35, 3600)
                .ask(new RagDtos.AskRequest("What is Spring Boot used for?", 3));

        assertEquals(java.util.List.of(0.1f, 0.2f, 0.3f), response.queryEmbedding());
        assertEquals(1, response.contexts().size());
        assertEquals(42L, response.contexts().get(0).vectorId());
        assertEquals(42L, response.contexts().get(0).chunkId());
        assertEquals(0.1234, response.contexts().get(0).distance());
        verify(ollama, times(1)).embed("What is Spring Boot used for?");
        verify(documents).search(same(embedding), eq(3));
    }

    /**
     * Executes rejectsResultsOutsideTheRelevanceThreshold.
     */
    @Test
    void rejectsResultsOutsideTheRelevanceThreshold() {
        DocumentService documents = mock(DocumentService.class);
        OllamaService ollama = mock(OllamaService.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenReturn(null);

        float[] embedding = {0.1f, 0.2f, 0.3f};
        when(ollama.embed("Unrelated question")).thenReturn(embedding);
        when(ollama.generationModel()).thenReturn("test-model");
        when(documents.search(same(embedding), eq(3))).thenReturn(java.util.List.of(
                new DocumentDtos.ChunkResult(1L, 1L, 1L, "Unrelated", 0, "Not relevant", 0.3501),
                new DocumentDtos.ChunkResult(2L, 2L, 2L, "Relevant", 0, "Relevant enough", 0.35)
        ));

        RagDtos.AskResponse response = new RagService(documents, ollama, redis, new ObjectMapper(), 0.35, 3600)
                .ask(new RagDtos.AskRequest("Unrelated question", 3));

        assertEquals(1, response.contexts().size());
        assertEquals(2L, response.contexts().get(0).vectorId());
        verify(ollama).generate(contains("Relevant enough"));
    }

    /**
     * Executes returnsExplicitMessageWhenNoResultPassesThreshold.
     */
    @Test
    void returnsExplicitMessageWhenNoResultPassesThreshold() {
        DocumentService documents = mock(DocumentService.class);
        OllamaService ollama = mock(OllamaService.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenReturn(null);

        float[] embedding = {0.1f, 0.2f, 0.3f};
        when(ollama.embed("Unknown question")).thenReturn(embedding);
        when(ollama.generationModel()).thenReturn("test-model");
        when(documents.search(same(embedding), eq(3))).thenReturn(java.util.List.of(
                new DocumentDtos.ChunkResult(1L, 1L, 1L, "Unrelated", 0, "Not relevant", 0.8)
        ));

        RagDtos.AskResponse response = new RagService(documents, ollama, redis, new ObjectMapper(), 0.35, 3600)
                .ask(new RagDtos.AskRequest("Unknown question", 3));

        assertEquals("No sufficiently relevant documents found.", response.answer());
        assertTrue(response.contexts().isEmpty());
        verify(ollama, never()).generate(anyString());
    }
}
