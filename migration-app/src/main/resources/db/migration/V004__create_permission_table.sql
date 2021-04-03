CREATE TABLE permission (
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    resource_id VARCHAR(36) NOT NULL,
    permission_type VARCHAR(16) NOT NULL,

    CONSTRAINT fk_permission_user_id FOREIGN KEY (user_id) REFERENCES user_info(id),
    CONSTRAINT fk_permission_resource_id FOREIGN KEY (resource_id) REFERENCES user_info(id)
);