package org.example.virtualgene.domain;

import org.example.virtualgene.domain.DAO.Account;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface AccountRepository extends ReactiveCrudRepository<Account, UUID> {

    Flux<Account> findByEmail(String email);

    Flux<Account> findByEmailAndPasswordNotNull(String email);

    Mono<Long> countByEmail(String email);
}