package com.tiltedev.springreactive.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Tuning for {@code IssService}. Bound from {@code app.iss.*}. */
@Data
@Component
@ConfigurationProperties(prefix = "app.iss")
public class IssProperties {

  /** How often the ISS position is polled from Open Notify and pushed over the WebSocket. */
  private Duration pollInterval = Duration.ofSeconds(30);
}
