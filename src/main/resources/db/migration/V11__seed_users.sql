INSERT INTO users (id, username, created_at, updated_at, user_account_status, cohort, version)
VALUES
    (1, 'alice', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE', 'STANDARD', 0),
    (2, 'bob', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE', 'EARLY_ADOPTER', 0),
    (3, 'carol', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE', 'EMPLOYEE', 0),
    (4, 'dave', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'DEACTIVATED', 'STANDARD', 0),
    (5, 'eve', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE', 'EARLY_ADOPTER', 0);

SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
