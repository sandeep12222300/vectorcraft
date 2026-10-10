package com.yourownai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourownai.dto.DocumentDtos;
import com.yourownai.dto.RagDtos;
import com.yourownai.service.RagService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Provides RagControllerTest behavior.
 */
class RagControllerTest {
    /**
     * Executes askReturnsEmbeddingAndRetrievedVectorDetails.
     */
    @Test
    void askReturnsEmbeddingAndRetrievedVectorDetails() throws Exception {
        RagService service = mock(RagService.class);
        when(service.ask(any())).thenReturn(new RagDtos.AskResponse(
                "It builds Spring applications.",
                "test-model",
                List.of(0.1f, 0.2f, 0.3f),
                List.of(new DocumentDtos.ChunkResult(42L, 42L, 7L, "Spring", 0, "Spring context", 0.1234))
        ));
        MockMvc mvc = standaloneSetup(new RagController(service)).build();

        mvc.perform(post("/api/v1/rag/ask")
                        .contentType("application/json")
                        .content("""
                                {"question":"What is Spring Boot used for?","k":3}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("It builds Spring applications."))
                .andExpect(jsonPath("$.model").value("test-model"))
                .andExpect(jsonPath("$.queryEmbedding.length()").value(3))
                .andExpect(jsonPath("$.contexts[0].vectorId").value(42))
                .andExpect(jsonPath("$.contexts[0].chunkId").value(42))
                .andExpect(jsonPath("$.contexts[0].title").value("Spring"))
                .andExpect(jsonPath("$.contexts[0].distance").value(0.1234));
    }
}
