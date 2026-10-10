# VectorCraft frontend implementation notes

## Scope

This update changes the static frontend and README only. Java controllers, DTOs, services, vector algorithms, persistence mappings, and Flyway migrations were not modified.

## Added

- Top-level **Learn AI** and **AI Lab** workspaces.
- Beginner-friendly concept lessons: LLMs, tokens/context windows, embeddings, RAG, vector search, hallucinations, prompt design, and RAG vs fine-tuning.
- Guided RAG pipeline walkthrough that is updated from the real `/api/v1/rag/ask` response.
- Prompt playground that calls the existing RAG API, rather than inventing a generic chat endpoint.
- Evidence lab showing retrieved chunk content and distances from the real response.
- Blue-and-white styling and responsive layout for smaller screens.
- Safer HTML rendering for model responses, document titles, and retrieved passages.
- Separate plot projections for manual demo vectors and Ollama document embeddings, which can have different dimensions.
- Clear explanation that the 2D projection is approximate and similarity/distance is not a confidence percentage.
- Better errors for failed document, vector and benchmark actions.

## Existing backend contract used

- `GET /api/v1/status`
- `GET /api/v1/vectors`
- `GET /api/v1/documents/vectors`
- `POST /api/v1/vectors`
- `POST /api/v1/vectors/search`
- `DELETE /api/v1/vectors/{id}`
- `GET /api/v1/vectors/benchmark`
- `POST /api/v1/documents`
- `GET /api/v1/documents`
- `DELETE /api/v1/documents/{id}`
- `POST /api/v1/rag/ask`

## Validation performed

- HTML parser check: passed.
- Duplicate element IDs check: passed.
- Inline JavaScript syntax check using `node --check`: passed.
- Static checks for existing backend route references and new learning modules: passed.
- Maven tests: not run in this environment because `mvn` is not installed. Run `mvn clean test` from the project root before considering the project fully regression-tested.

## Known limitations

- The prompt playground uses the existing RAG endpoint and indexed documents; it is not a general-purpose prompt playground without document context.
- The backend does not expose a separate no-context generation endpoint, so the UI does not pretend to provide a live A/B comparison of RAG versus plain LLM answers.
- The manual vector playground still uses the existing illustrative client-side 16-dimensional keyword embedding. It is explicitly separate from real Ollama document embeddings.
- The visualization shows the backend's actual query embedding only when `queryEmbedding` is included in the RAG response and compatible document vectors are available.
