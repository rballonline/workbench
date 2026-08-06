package com.tiltedev.spring_reactive.repository;

import com.tiltedev.spring_reactive.model.Destination;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationRepository extends ReactiveCrudRepository<Destination, Long> {
}
