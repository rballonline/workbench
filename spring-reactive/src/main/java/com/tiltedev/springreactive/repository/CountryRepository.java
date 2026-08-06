package com.tiltedev.springreactive.repository;

import com.tiltedev.springreactive.model.Country;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends ReactiveCrudRepository<Country, String> {}
