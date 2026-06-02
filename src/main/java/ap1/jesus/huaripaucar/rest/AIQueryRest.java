package ap1.jesus.huaripaucar.rest;

import ap1.jesus.huaripaucar.model.AIQuery;
import ap1.jesus.huaripaucar.model.dto.ChatGptRequest;
import ap1.jesus.huaripaucar.model.dto.SummarizerRequest;
import ap1.jesus.huaripaucar.service.AIQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/api/ai")
public class AIQueryRest {

    private final AIQueryService service;

    @Autowired
    public AIQueryRest(AIQueryService service) {
        this.service = service;
    }


    @PostMapping("/chatgpt")
    public Mono<AIQuery> askChatGpt(@RequestBody ChatGptRequest request) {
        return service.askChatGpt(request);
    }

    @PostMapping("/summarizer")
    public Mono<AIQuery> summarizeUrl(@RequestBody SummarizerRequest request) {
        return service.summarizeUrl(request);
    }


    @GetMapping
    public Flux<AIQuery> findAll() {
        return service.findAll();
    }

    @GetMapping("/active")
    public Flux<AIQuery> findActive() {
        return service.findActive();
    }

    @GetMapping("/inactive")
    public Flux<AIQuery> findInactive() {
        return service.findInactive();
    }

    @GetMapping("/api/{apiName}")
    public Flux<AIQuery> findByApi(@PathVariable String apiName) {
        return service.findByApi(apiName);
    }

    @GetMapping("/{id}")
    public Mono<AIQuery> findById(@PathVariable String id) {
        return service.findById(id);
    }


    @PutMapping("/update/{id}")
    public Mono<AIQuery> update(@PathVariable String id, @RequestBody AIQuery aiQuery) {
        return service.update(id, aiQuery);
    }


    @PatchMapping("/delete/{id}")
    public Mono<AIQuery> delete(@PathVariable String id) {
        return service.delete(id);
    }

    @PatchMapping("/restore/{id}")
    public Mono<AIQuery> restore(@PathVariable String id) {
        return service.restore(id);
    }

}