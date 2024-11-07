package com.waypoint.resourcehub.repository;

import com.waypoint.resourcehub.domain.entity.FileProcessingStatus;
import com.waypoint.resourcehub.domain.entity.RefTerm;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface FileProcessingStatusRepository extends ReactiveCrudRepository<FileProcessingStatus, UUID> {
    Mono<Boolean> existsByFileNameAndStatus(String fileName, String status);
}
