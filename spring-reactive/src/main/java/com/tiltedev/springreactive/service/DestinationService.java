package com.tiltedev.spring_reactive.service;

import com.tiltedev.spring_reactive.dto.event.DestinationEvent;
import com.tiltedev.spring_reactive.dto.projection.DestinationWithCountry;
import com.tiltedev.spring_reactive.dto.request.AddDestinationRequest;
import com.tiltedev.spring_reactive.dto.response.CountryResponse;
import com.tiltedev.spring_reactive.dto.response.DestinationResponse;
import com.tiltedev.spring_reactive.model.Destination;
import com.tiltedev.spring_reactive.repository.DestinationCountryRepository;
import com.tiltedev.spring_reactive.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Slf4j
@Service
@RequiredArgsConstructor
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final DestinationCountryRepository destinationCountryRepository;
    private final CountrySyncService countrySyncService;
    private final Sinks.Many<DestinationEvent> eventSink;

    public Flux<DestinationResponse> findAll() {
        return destinationCountryRepository.findAll().map(this::toResponse);
    }

    public Mono<DestinationResponse> findById(Long id) {
        return destinationCountryRepository.findById(id)
                .map(this::toResponse)
                .switchIfEmpty(Mono.error(new RuntimeException("Destination not found: " + id)));
    }

    public Mono<DestinationResponse> create(AddDestinationRequest request) {
        return countrySyncService.ensureExists(request.getCountryCode())
                .flatMap(country -> {
                    Destination destination = Destination.builder()
                            .cityName(request.getCityName())
                            .countryCode(request.getCountryCode().toUpperCase())
                            .latitude(request.getLatitude())
                            .longitude(request.getLongitude())
                            .addedBy(request.getAddedBy())
                            .build();
                    return destinationRepository.save(destination);
                })
                .flatMap(saved -> {
                    log.info("Destination created: {} (id={})", saved.getCityName(), saved.getId());
                    return findById(saved.getId())
                            .doOnSuccess(response -> eventSink.tryEmitNext(
                                    new DestinationEvent(DestinationEvent.Action.CREATED, saved)));
                });
    }

    public Mono<Void> delete(Long id) {
        return destinationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Destination not found: " + id)))
                .flatMap(destination -> destinationRepository.deleteById(id)
                        .doOnSuccess(v -> {
                            log.info("Destination deleted: {} (id={})", destination.getCityName(), id);
                            eventSink.tryEmitNext(new DestinationEvent(DestinationEvent.Action.DELETED, destination));
                        })
                );
    }

    private DestinationResponse toResponse(DestinationWithCountry d) {
        return DestinationResponse.builder()
                .id(d.getId())
                .cityName(d.getCityName())
                .latitude(d.getLatitude())
                .longitude(d.getLongitude())
                .addedBy(d.getAddedBy())
                .createdAt(d.getCreatedAt())
                .country(CountryResponse.builder()
                        .code(d.getCountryCode())
                        .name(d.getCountryName())
                        .capital(d.getCapital())
                        .region(d.getRegion())
                        .population(d.getPopulation())
                        .flagUrl(d.getFlagUrl())
                        .build())
                .build();
    }
}
