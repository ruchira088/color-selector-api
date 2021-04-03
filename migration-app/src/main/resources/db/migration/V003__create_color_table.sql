CREATE TABLE color (
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    color_value VARCHAR(10) NOT NULL,

    CONSTRAINT fk_color_user_id FOREIGN KEY (user_id) REFERENCES user_info(id),
    PRIMARY KEY (user_id)
);