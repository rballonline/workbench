package com.tiltedev.spring_reactive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient geocodingWebClient() {
        return WebClient.builder()
                .baseUrl("https://geocoding-api.open-meteo.com")
                .build();
    }

    @Bean
    public WebClient forecastWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.open-meteo.com")
                .build();
    }

    @Bean
    public WebClient countriesWebClient() {
        return WebClient.builder()
                .baseUrl("https://restcountries.com")
                .build();
    }

    @Bean
    public WebClient issWebClient() {
        return WebClient.builder()
                .baseUrl("http://api.open-notify.org")
                .build();
    }

    /**
     * OAuth2-authenticated client for machine-to-machine calls.
     * Configure spring.security.oauth2.client.* properties to point at your provider.
     * This client fetches and caches a client-credentials token automatically.
     */
    @Bean
    public WebClient oAuth2WebClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2.setDefaultClientRegistrationId("my-api");

        return WebClient.builder()
                .filter(oauth2)
                .build();
    }
}
