-- Seed default admin user
-- Password: Admin@123 (BCrypt encoded)
INSERT INTO users (first_name, last_name, email, password, role_id)
VALUES (
    'System',
    'Admin',
    'admin@ticketbooking.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    (SELECT id FROM roles WHERE name = 'ADMIN')
);
