package com.tiltedev.springreactive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient geocodingWebClient() {
    return WebClient.builder().baseUrl("https://geocoding-api.open-meteo.com").build();
  }

  @Bean
  public WebClient forecastWebClient() {
    return WebClient.builder().baseUrl("https://api.open-meteo.com").build();
  }

  @Bean
  public WebClient countriesWebClient(@Value("${app.restcountries.api-key}") String apiKey) {
    return WebClient.builder()
        .baseUrl("https://api.restcountries.com")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .build();
  }

  @Bean
  public WebClient issWebClient() {
    return WebClient.builder().baseUrl("http://api.open-notify.org").build();
  }

  /**
   * Base client for Anthropic calls - no {@code x-api-key} header here since that is
   * bring-your-own-key per request, not app config. Callers mutate in the header per call.
   */
  @Bean
  public WebClient anthropicWebClient() {
    return WebClient.builder()
        .baseUrl("https://api.anthropic.com")
        .defaultHeader("anthropic-version", "2023-06-01")
        .build();
  }

  /**
   * OAuth2-authenticated client for machine-to-machine calls. Configure
   * spring.security.oauth2.client.* properties to point at your provider. This client fetches and
   * caches a client-credentials token automatically.
   */
  @Bean
  public WebClient oAuth2WebClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
    ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
        new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
    oauth2.setDefaultClientRegistrationId("my-api");

    return WebClient.builder().filter(oauth2).build();
  }
}
