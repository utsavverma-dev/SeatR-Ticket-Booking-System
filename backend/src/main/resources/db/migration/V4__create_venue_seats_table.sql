CREATE TABLE venue_seats (
    id BIGSERIAL PRIMARY KEY,
    venue_id BIGINT NOT NULL,
    category VARCHAR(20) NOT NULL,
    seat_label VARCHAR(10) NOT NULL,
    row_number VARCHAR(5) NOT NULL,
    seat_number INT NOT NULL,
    CONSTRAINT fk_venue_seats_venue FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE,
    CONSTRAINT uq_venue_seat_label UNIQUE (venue_id, seat_label)
);

CREATE INDEX idx_venue_seats_venue_id ON venue_seats(venue_id);
