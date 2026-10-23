IF OBJECT_ID(N'dbo.users', N'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.users', 'email_verified') IS NULL
        ALTER TABLE dbo.users ADD email_verified bit NOT NULL CONSTRAINT DF_users_email_verified DEFAULT (1);
    IF COL_LENGTH('dbo.users', 'organization_approved') IS NULL
        ALTER TABLE dbo.users ADD organization_approved bit NOT NULL CONSTRAINT DF_users_organization_approved DEFAULT (1);
    IF COL_LENGTH('dbo.users', 'organization_approved_at') IS NULL
        ALTER TABLE dbo.users ADD organization_approved_at datetimeoffset NULL;
    IF COL_LENGTH('dbo.users', 'organization_approved_by') IS NULL
        ALTER TABLE dbo.users ADD organization_approved_by bigint NULL;
    IF COL_LENGTH('dbo.users', 'guardian_consent_verified') IS NULL
        ALTER TABLE dbo.users ADD guardian_consent_verified bit NOT NULL CONSTRAINT DF_users_guardian_consent_verified DEFAULT (1);
    IF COL_LENGTH('dbo.users', 'guardian_consent_verified_at') IS NULL
        ALTER TABLE dbo.users ADD guardian_consent_verified_at datetimeoffset NULL;
    IF COL_LENGTH('dbo.users', 'verification_email_sent_at') IS NULL
        ALTER TABLE dbo.users ADD verification_email_sent_at datetimeoffset NULL;
    IF COL_LENGTH('dbo.users', 'guardian_consent_email_sent_at') IS NULL
        ALTER TABLE dbo.users ADD guardian_consent_email_sent_at datetimeoffset NULL;
END;

IF OBJECT_ID(N'dbo.rate_limit_buckets', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.rate_limit_buckets (
        bucket_key char(64) NOT NULL,
        window_start bigint NOT NULL,
        request_count int NOT NULL,
        CONSTRAINT PK_rate_limit_buckets PRIMARY KEY (bucket_key, window_start)
    );
END;

IF OBJECT_ID(N'dbo.account_verification_tokens', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.account_verification_tokens (
        id bigint IDENTITY(1,1) NOT NULL CONSTRAINT PK_account_verification_tokens PRIMARY KEY,
        user_id bigint NOT NULL,
        token_hash varchar(64) NOT NULL,
        token_type varchar(20) NOT NULL,
        destination_email varchar(254) NOT NULL,
        expires_at datetimeoffset NOT NULL,
        consumed_at datetimeoffset NULL,
        CONSTRAINT FK_verification_tokens_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
        CONSTRAINT UQ_verification_token_hash UNIQUE (token_hash)
    );
    CREATE INDEX idx_verification_user_type ON dbo.account_verification_tokens(user_id, token_type);
END;
