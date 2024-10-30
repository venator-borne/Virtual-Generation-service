package org.example.virtualgene.domain;

import org.example.virtualgene.common.enums.ResourcesType;
import org.example.virtualgene.domain.DAO.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ResourceRepository extends ReactiveCrudRepository<Resource, Long> {

    public Mono<Resource> findByIdentifier(String identifier);

    public Flux<Resource> findByAccountIdOrAccessIs(Pageable pageable, UUID accountId, Boolean access);
}
