package com.tiltedev.spring_reactive.repository;

import com.tiltedev.spring_reactive.model.Country;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends ReactiveCrudRepository<Country, String> {
}
