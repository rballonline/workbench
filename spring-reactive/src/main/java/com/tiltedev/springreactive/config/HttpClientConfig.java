package com.tiltedev.springreactive.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Tuning for {@code ReactiveHttpClient}. Bound from {@code app.http-client.*}. */
@Data
@Component
@ConfigurationProperties(prefix = "app.http-client")
public class HttpClientConfig {

    /** Per-attempt request timeout. Each retry attempt gets a fresh timeout. */
    private Duration timeout = Duration.ofSeconds(10);

    private RetryProperties retry = new RetryProperties();

    @Data
    public static class RetryProperties {

        /** Master switch - when false, a failed call propagates on the first attempt. */
        private boolean enabled = true;

        /** Retries attempted <em>after</em> the initial call. 2 means up to 3 total calls. */
        private int maxRetries = 2;

        /** Delay before the first retry; doubles on each subsequent retry. */
        private Duration initialBackoff = Duration.ofMillis(200);

        /** Ceiling for the exponential backoff. */
        private Duration maxBackoff = Duration.ofSeconds(2);

        /** Randomness applied to each backoff, 0.0–1.0, to avoid thundering herds. */
        private double jitter = 0.5;

        /**
         * When true, only GET/PUT/DELETE are retried. POST is left alone because a request that
         * timed out may still have been applied on the far side.
         */
        private boolean idempotentOnly = true;
    }
}
