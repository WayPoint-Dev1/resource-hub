package com.waypoint.resourcehub.repository;

import com.waypoint.resourcehub.domain.entity.RefTerm;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RefTermRepository extends ReactiveCrudRepository<RefTerm, UUID> {
  Mono<RefTerm> findByIdAndIsActive(UUID id, Boolean isActive);
  Mono<RefTerm> findByIdAndRefSetIdAndIsActive(UUID id, UUID refSetId, Boolean isActive);

  Mono<RefTerm> findByRefTermIdAndRefSetIdAndIsActive(
      Integer refTermId, UUID refSetId, Boolean isActive);

  Mono<RefTerm> findByRefTermIdAndIsActive(
          Integer refTermId, Boolean isActive);

  Flux<RefTerm> findAllByRefTermNameLikeAndRefSetIdAndIsActive(
      String refTermName, UUID refSetId, Boolean isActive);

  Flux<RefTerm> findByRefTermNameLikeAndIsActive(
          String refTermName, Boolean isActive);

  Flux<RefTerm> findAllByRefSetIdAndIsActive(UUID refSetId, Boolean isActive);
}
