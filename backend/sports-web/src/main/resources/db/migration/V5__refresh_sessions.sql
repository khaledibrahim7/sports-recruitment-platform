CREATE TABLE auth_refresh_sessions (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    expires_at DATETIMEOFFSET NOT NULL,
    user_agent NVARCHAR(512) NULL,
    CONSTRAINT fk_auth_refresh_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ux_auth_refresh_token_hash UNIQUE (token_hash)
);

CREATE INDEX ix_auth_refresh_user_id ON auth_refresh_sessions(user_id);
CREATE INDEX ix_auth_refresh_expires_at ON auth_refresh_sessions(expires_at);
