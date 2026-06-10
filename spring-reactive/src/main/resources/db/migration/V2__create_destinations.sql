CREATE TABLE destinations (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    city_name    VARCHAR(255) NOT NULL,
    country_code VARCHAR(10),
    latitude     DOUBLE,
    longitude    DOUBLE,
    added_by     VARCHAR(100),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_destination_country FOREIGN KEY (country_code) REFERENCES countries (code)
);
