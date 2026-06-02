package ap1.jesus.huaripaucar.service;

import ap1.jesus.huaripaucar.model.AIQuery;
import ap1.jesus.huaripaucar.model.dto.ChatGptRequest;
import ap1.jesus.huaripaucar.model.dto.SummarizerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AIQueryService {

    Mono<AIQuery> askChatGpt(ChatGptRequest request);
    Mono<AIQuery> summarizeUrl(SummarizerRequest request);

    Flux<AIQuery> findAll();
    Flux<AIQuery> findActive();
    Flux<AIQuery> findInactive();
    Flux<AIQuery> findByApi(String apiName);
    Mono<AIQuery>  findById(String id);

    Mono<AIQuery> update(String id, AIQuery aiQuery);

    Mono<AIQuery> delete(String id);
    Mono<AIQuery> restore(String id);

}