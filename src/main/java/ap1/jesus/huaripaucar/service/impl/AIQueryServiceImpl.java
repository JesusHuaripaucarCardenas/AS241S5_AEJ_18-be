package ap1.jesus.huaripaucar.service.impl;

import ap1.jesus.huaripaucar.model.AIQuery;
import ap1.jesus.huaripaucar.model.dto.ChatGptRequest;
import ap1.jesus.huaripaucar.model.dto.SummarizerRequest;
import ap1.jesus.huaripaucar.repository.AIQueryRepository;
import ap1.jesus.huaripaucar.service.AIQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AIQueryServiceImpl implements AIQueryService {

    private final WebClient webClient;
    private final AIQueryRepository repository;

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    @Value("${rapidapi.chatgpt.host}")
    private String chatGptHost;

    @Value("${rapidapi.chatgpt.url}")
    private String chatGptUrl;

    @Value("${rapidapi.summarizer.host}")
    private String summarizerHost;

    @Value("${rapidapi.summarizer.url}")
    private String summarizerUrl;

    public AIQueryServiceImpl(WebClient.Builder builder, AIQueryRepository repository) {
        this.webClient = builder.build();
        this.repository = repository;
    }

    private Mono<String> generarSiguienteId() {
        return repository.findAll()
                .map(q -> {
                    try {
                        return Integer.parseInt(q.getId().replace("AI-", ""));
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .reduce(0, Integer::max)
                .map(max -> "AI-" + (max + 1));
    }

    @Override
    public Mono<AIQuery> askChatGpt(ChatGptRequest request) {
        log.info("Llamando a ChatGPT con prompt: {}", request.getPrompt());

        String uri = UriComponentsBuilder
                .fromHttpUrl(chatGptUrl)
                .queryParam("prompt", request.getPrompt())
                .toUriString();

        return webClient.get()
                .uri(uri)
                .header("x-rapidapi-key", rapidApiKey)
                .header("x-rapidapi-host", chatGptHost)
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(json -> {
                    String response = extractChatGptResponse(json);
                    return generarSiguienteId().flatMap(id -> {
                        AIQuery query = new AIQuery();
                        query.setId(id);
                        query.setApiName("ChatGPT");
                        query.setPrompt(request.getPrompt());
                        query.setResponse(response);
                        query.setEstado("A");
                        query.setCreatedAt(LocalDateTime.now());
                        query.setUpdatedAt(LocalDateTime.now());
                        return repository.save(query);
                    });
                })
                .doOnError(e -> log.error("Error en ChatGPT: {}", e.getMessage()));
    }

    private String extractChatGptResponse(JsonNode json) {
        try {
            if (json.has("result"))  return json.get("result").asText();
            if (json.has("choices")) return json.get("choices").get(0)
                    .get("message").get("content").asText();
            return json.toString();
        } catch (Exception e) {
            return json.toString();
        }
    }

    @Override
    public Mono<AIQuery> summarizeUrl(SummarizerRequest request) {
        log.info("Llamando a Summarizer con url: {}", request.getUrl());

        String uri = UriComponentsBuilder
                .fromHttpUrl(summarizerUrl + "/summarize")
                .queryParam("url", request.getUrl())
                .queryParam("lang", request.getLang())
                .queryParam("engine", request.getEngine())
                .toUriString();

        return webClient.get()
                .uri(uri)
                .header("x-rapidapi-key", rapidApiKey)
                .header("x-rapidapi-host", summarizerHost)
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(json -> {
                    String response = extractSummarizerResponse(json);
                    return generarSiguienteId().flatMap(id -> {
                        AIQuery query = new AIQuery();
                        query.setId(id);
                        query.setApiName("Summarizer");
                        query.setPrompt("Resumen de: " + request.getUrl());
                        query.setResponse(response);
                        query.setUrl(request.getUrl());
                        query.setLang(request.getLang());
                        query.setEngine(request.getEngine());
                        query.setEstado("A");
                        query.setCreatedAt(LocalDateTime.now());
                        query.setUpdatedAt(LocalDateTime.now());
                        return repository.save(query);
                    });
                })
                .doOnError(e -> log.error("Error en Summarizer: {}", e.getMessage()));
    }

    private String extractSummarizerResponse(JsonNode json) {
        try {
            if (json.has("summary")) return json.get("summary").asText();
            if (json.isArray() && json.size() > 0) return json.get(0).asText();
            return json.toString();
        } catch (Exception e) {
            return json.toString();
        }
    }

    @Override
    public Flux<AIQuery> findAll() {
        log.info("Listando todas las consultas IA");
        return repository.findAll();
    }

    @Override
    public Flux<AIQuery> findActive() {
        log.info("Listando consultas activas");
        return repository.findByEstado("A");
    }

    @Override
    public Flux<AIQuery> findInactive() {
        log.info("Listando consultas inactivas");
        return repository.findByEstado("I");
    }

    @Override
    public Flux<AIQuery> findByApi(String apiName) {
        log.info("Listando consultas por API: {}", apiName);
        return repository.findByApiName(apiName);
    }

    @Override
    public Mono<AIQuery> findById(String id) {
        log.info("Buscando consulta por ID: {}", id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Consulta no encontrada con ID: " + id)));
    }

    @Override
    public Mono<AIQuery> update(String id, AIQuery aiQuery) {
        log.info("Actualizando consulta ID: {}", id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Consulta no encontrada con ID: " + id)))
                .flatMap(existing -> {
                    if ("I".equals(existing.getEstado())) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "No se puede editar una consulta inactiva (ID: " + id + ")"));
                    }
                    existing.setPrompt(aiQuery.getPrompt());
                    existing.setUrl(aiQuery.getUrl());
                    existing.setLang(aiQuery.getLang());
                    existing.setEngine(aiQuery.getEngine());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                });
    }

    @Override
    public Mono<AIQuery> delete(String id) {
        log.info("Eliminando lógicamente consulta ID: {}", id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Consulta no encontrada con ID: " + id)))
                .flatMap(existing -> {
                    if ("I".equals(existing.getEstado())) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "La consulta ya está inactiva (ID: " + id + ")"));
                    }
                    existing.setEstado("I");
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                });
    }


    @Override
    public Mono<AIQuery> restore(String id) {
        log.info("Restaurando consulta ID: {}", id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Consulta no encontrada con ID: " + id)))
                .flatMap(existing -> {
                    if ("A".equals(existing.getEstado())) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "La consulta ya está activa (ID: " + id + ")"));
                    }
                    existing.setEstado("A");
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                });
    }

}