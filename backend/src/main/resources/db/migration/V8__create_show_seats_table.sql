CREATE TABLE show_seats (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    venue_seat_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    held_by_user_id BIGINT,
    hold_expires_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_show_seats_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_show_seats_venue_seat FOREIGN KEY (venue_seat_id) REFERENCES venue_seats(id),
    CONSTRAINT fk_show_seats_held_by FOREIGN KEY (held_by_user_id) REFERENCES users(id),
    CONSTRAINT uq_show_seats_event_seat UNIQUE (event_id, venue_seat_id)
);

CREATE INDEX idx_show_seats_event_id ON show_seats(event_id);
CREATE INDEX idx_show_seats_status ON show_seats(status);
CREATE INDEX idx_show_seats_hold_expires_at ON show_seats(hold_expires_at);
