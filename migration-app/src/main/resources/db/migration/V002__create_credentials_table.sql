CREATE TABLE credentials (
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NUll,
    salted_hashed_password VARCHAR(128) NOT NULL,

    CONSTRAINT fk_credentials_user_id FOREIGN KEY (user_id) REFERENCES user_info(id),
    PRIMARY KEY (user_id)
);