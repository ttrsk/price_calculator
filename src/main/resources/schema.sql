CREATE TABLE INSTRUMENT_PRICE_MODIFIER (
    ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME VARCHAR(128) NOT NULL,
    MULTIPLIER DOUBLE PRECISION NOT NULL
);