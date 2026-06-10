CREATE TABLE countries (
    code       VARCHAR(10)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    capital    VARCHAR(255),
    region     VARCHAR(255),
    population BIGINT,
    flag_url   VARCHAR(500),
    PRIMARY KEY (code)
);
