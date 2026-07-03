CREATE TABLE event_prices (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    category VARCHAR(20) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_event_prices_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT uq_event_price_category UNIQUE (event_id, category)
);

CREATE INDEX idx_event_prices_event_id ON event_prices(event_id);
