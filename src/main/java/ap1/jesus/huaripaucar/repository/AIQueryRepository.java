package ap1.jesus.huaripaucar.repository;

import ap1.jesus.huaripaucar.model.AIQuery;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface AIQueryRepository extends ReactiveMongoRepository<AIQuery, String> {

    Flux<AIQuery> findByEstado(String estado);

    Flux<AIQuery> findByApiName(String apiName);

    Flux<AIQuery> findByApiNameAndEstado(String apiName, String estado);

}