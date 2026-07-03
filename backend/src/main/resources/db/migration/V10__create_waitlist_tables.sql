CREATE TABLE waitlist (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    seat_category VARCHAR(50) NOT NULL,
    position_in_queue INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE (event_id, user_id, seat_category)
);

CREATE TABLE waitlist_offers (
    id BIGSERIAL PRIMARY KEY,
    waitlist_id BIGINT NOT NULL REFERENCES waitlist(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE waitlist_offer_seats (
    offer_id BIGINT NOT NULL REFERENCES waitlist_offers(id) ON DELETE CASCADE,
    show_seat_id BIGINT NOT NULL REFERENCES show_seats(id) ON DELETE CASCADE,
    PRIMARY KEY (offer_id, show_seat_id)
);

CREATE INDEX idx_waitlist_event_category ON waitlist(event_id, seat_category, status);
CREATE INDEX idx_waitlist_offers_token ON waitlist_offers(token);
