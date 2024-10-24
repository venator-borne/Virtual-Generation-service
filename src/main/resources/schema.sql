DROP TABLE IF EXISTS account CASCADE;
CREATE TABLE IF NOT EXISTS account
(
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name  VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    roles VARCHAR(255)
);
