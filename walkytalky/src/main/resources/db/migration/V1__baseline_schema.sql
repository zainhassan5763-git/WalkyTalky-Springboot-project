-- V1__baseline_schema.sql

CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    avatar_url    VARCHAR(500) DEFAULT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    provider      VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    provider_id   VARCHAR(255)          DEFAULT NULL,
    email_verified BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version       INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;




CREATE TABLE tenants (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    slug       VARCHAR(100) NOT NULL,
    name       VARCHAR(150) NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    plan       VARCHAR(30)  NOT NULL DEFAULT 'FREE',
    seat_limit INT          NOT NULL DEFAULT 5,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version    INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenants_slug (slug)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;




CREATE TABLE tenant_memberships (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    tenant_id  CHAR(36)    NOT NULL,
    role       VARCHAR(30) NOT NULL DEFAULT 'MEMBER',
    joined_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_membership (user_id, tenant_id),
    KEY idx_membership_user (user_id),
    KEY idx_membership_tenant (tenant_id),
    CONSTRAINT fk_membership_user   FOREIGN KEY (user_id)   REFERENCES users (id)   ON DELETE CASCADE,
    CONSTRAINT fk_membership_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;



CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    refresh_token VARCHAR(500) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);



CREATE TABLE channels (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    is_private  BOOLEAN      NOT NULL DEFAULT FALSE,
    tenant_id   BIGINT       NOT NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_channels_tenant_name (tenant_id, name),
    KEY idx_channels_tenant (tenant_id),
    CONSTRAINT fk_channels_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;




CREATE TABLE messages (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    content    TEXT        NOT NULL,
    sender_id  BIGINT      NOT NULL,
    channel_id BIGINT      NOT NULL,
    is_edited  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_messages_channel_created (channel_id, created_at DESC),
    KEY idx_messages_sender (sender_id),
    CONSTRAINT fk_messages_sender  FOREIGN KEY (sender_id)  REFERENCES users (id)    ON DELETE CASCADE,
    CONSTRAINT fk_messages_channel FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;


