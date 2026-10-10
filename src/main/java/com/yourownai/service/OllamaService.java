package com.yourownai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * Provides OllamaService behavior.
 */
@Service
public class OllamaService {
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String embeddingModel;
    private final String generationModel;

    /**
     * Executes OllamaService.
     */
    public OllamaService(@Value("${ollama.base-url:http://localhost:11434}") String baseUrl, @Value("${ollama.embedding-model:nomic-embed-text}") String embeddingModel, @Value("${ollama.generation-model:llama3.2}") String generationModel, @Value("${ollama.timeout-seconds:120}") int timeoutSeconds, ObjectMapper mapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutSeconds * 1000);
        factory.setReadTimeout(timeoutSeconds * 1000);
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.embeddingModel = embeddingModel;
        this.generationModel = generationModel;
        this.mapper = mapper;
    }

    /**
     * Executes embed.
     */
    public float[] embed(String text) {
        try {
            String body = client.post().uri("/api/embeddings").contentType(MediaType.APPLICATION_JSON).body(Map.of("model", embeddingModel, "prompt", text)).retrieve().body(String.class);
            JsonNode n = mapper.readTree(body);
            JsonNode e = n.get("embedding");
            if (e == null) {
                throw new IllegalStateException("Ollama did not return an embedding");
            }
            float[] out = new float[e.size()];
            for (int i = 0; i < out.length; i++) {
                out[i] = (float) e.get(i).asDouble();
            }
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("Embedding request to Ollama failed: " + e.getMessage(), e);
        }
    }

    /**
     * Executes generate.
     */
    public String generate(String prompt) {
        try {
            String body = client.post().uri("/api/generate").contentType(MediaType.APPLICATION_JSON).body(Map.of("model", generationModel, "prompt", prompt, "stream", false)).retrieve().body(String.class);
            JsonNode n = mapper.readTree(body);
            return n.path("response").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Generation request to Ollama failed: " + e.getMessage(), e);
        }
    }

    /**
     * Executes generationModel.
     */
    public String generationModel() {
        return generationModel;
    }
}
