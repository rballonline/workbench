package com.tiltedev.spring_reactive.repository;

import com.tiltedev.spring_reactive.dto.projection.DestinationWithCountry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class DestinationCountryRepository {

    private final R2dbcEntityTemplate template;

    private static final String JOIN_SQL = """
            SELECT d.id, d.city_name, d.latitude, d.longitude, d.added_by, d.created_at,
                   c.code AS country_code, c.name AS country_name, c.capital, c.region,
                   c.population, c.flag_url
            FROM destinations d
            LEFT JOIN countries c ON d.country_code = c.code
            """;

    public Flux<DestinationWithCountry> findAll() {
        return template.getDatabaseClient()
                .sql(JOIN_SQL)
                .map((row, metadata) -> template.getConverter().read(DestinationWithCountry.class, row, metadata))
                .all();
    }

    public Mono<DestinationWithCountry> findById(Long id) {
        return template.getDatabaseClient()
                .sql(JOIN_SQL + "WHERE d.id = :id")
                .bind("id", id)
                .map((row, metadata) -> template.getConverter().read(DestinationWithCountry.class, row, metadata))
                .one();
    }
}
