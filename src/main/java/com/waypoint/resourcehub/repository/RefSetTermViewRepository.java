package com.waypoint.resourcehub.repository;

import com.waypoint.resourcehub.domain.entity.RefSetTermView;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefSetTermViewRepository extends ReactiveCrudRepository<RefSetTermView, UUID> {}
