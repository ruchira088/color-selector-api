CREATE TABLE user_info (
    id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    username VARCHAR(64) NOT NULL,
    first_name VARCHAR(128) NOT NULL,
    last_name VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL,

    PRIMARY KEY (id)
);