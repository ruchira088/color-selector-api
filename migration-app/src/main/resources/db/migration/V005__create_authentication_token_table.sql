CREATE TABLE authentication_token (
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    secret VARCHAR(36) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    renewals INTEGER NOT NULL,

    CONSTRAINT fk_authentication_token_user_id FOREIGN KEY (user_id) REFERENCES user_info(id)
);