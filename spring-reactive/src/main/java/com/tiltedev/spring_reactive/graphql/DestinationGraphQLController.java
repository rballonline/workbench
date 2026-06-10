package com.tiltedev.spring_reactive.graphql;

import com.tiltedev.spring_reactive.dto.request.AddDestinationRequest;
import com.tiltedev.spring_reactive.dto.response.DestinationResponse;
import com.tiltedev.spring_reactive.service.DestinationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DestinationGraphQLController {

    private final DestinationService service;

    @QueryMapping
    public Flux<DestinationResponse> destinations() {
        return service.findAll();
    }

    @QueryMapping
    public Mono<DestinationResponse> destination(@Argument Long id) {
        return service.findById(id);
    }

    @MutationMapping
    public Mono<DestinationResponse> addDestination(
            @Argument String cityName,
            @Argument String countryCode,
            @Argument Double latitude,
            @Argument Double longitude,
            @Argument String addedBy) {

        AddDestinationRequest request = new AddDestinationRequest();
        request.setCityName(cityName);
        request.setCountryCode(countryCode);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setAddedBy(addedBy);
        return service.create(request);
    }

    @MutationMapping
    public Mono<Boolean> removeDestination(@Argument Long id) {
        return service.delete(id).thenReturn(true);
    }
}
