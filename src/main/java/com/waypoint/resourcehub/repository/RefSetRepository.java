package com.waypoint.resourcehub.repository;

import java.util.UUID;

import com.waypoint.resourcehub.domain.entity.RefSet;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RefSetRepository extends ReactiveCrudRepository<RefSet, UUID> {
    Mono<RefSet> findByIdAndIsActive(UUID id, Boolean isActive);
    Mono<RefSet> findByRefSetIdAndIsActive(Integer refSetId, Boolean isActive);
    Flux<RefSet> findByRefSetNameLikeAndIsActive(String refSetName, Boolean isActive);

}
