package org.example.virtualgene.domain;

import org.example.virtualgene.domain.DAO.Model;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends ReactiveCrudRepository<Model, Long> {
}
