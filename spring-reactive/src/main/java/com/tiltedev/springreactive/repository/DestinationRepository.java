package com.tiltedev.springreactive.repository;

import com.tiltedev.springreactive.model.Destination;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationRepository extends ReactiveCrudRepository<Destination, Long> {}
