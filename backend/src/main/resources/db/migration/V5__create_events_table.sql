CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    venue_id BIGINT NOT NULL,
    organiser_id BIGINT NOT NULL,
    event_date DATE NOT NULL,
    event_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_events_venue FOREIGN KEY (venue_id) REFERENCES venues(id),
    CONSTRAINT fk_events_organiser FOREIGN KEY (organiser_id) REFERENCES users(id)
);

CREATE INDEX idx_events_venue_id ON events(venue_id);
CREATE INDEX idx_events_organiser_id ON events(organiser_id);
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_event_date ON events(event_date);
