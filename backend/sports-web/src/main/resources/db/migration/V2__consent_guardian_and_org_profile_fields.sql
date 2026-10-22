-- Existing installations are baselined at version 1. This migration adds only
-- fields introduced after the existing Hibernate-managed production schema.
IF OBJECT_ID(N'dbo.users', N'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.users', 'terms_accepted') IS NULL
        ALTER TABLE dbo.users ADD terms_accepted bit NOT NULL CONSTRAINT DF_users_terms_accepted DEFAULT (0);
    IF COL_LENGTH('dbo.users', 'terms_version') IS NULL
        ALTER TABLE dbo.users ADD terms_version nvarchar(40) NULL;
    IF COL_LENGTH('dbo.users', 'terms_accepted_at') IS NULL
        ALTER TABLE dbo.users ADD terms_accepted_at datetimeoffset NULL;
END;

IF OBJECT_ID(N'dbo.player_profiles', N'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.player_profiles', 'player_goal') IS NULL
        ALTER TABLE dbo.player_profiles ADD player_goal nvarchar(500) NULL;
    IF COL_LENGTH('dbo.player_profiles', 'available_for_transfer') IS NULL
        ALTER TABLE dbo.player_profiles ADD available_for_transfer bit NOT NULL CONSTRAINT DF_player_profiles_available_transfer DEFAULT (0);
    IF COL_LENGTH('dbo.player_profiles', 'guardian_name') IS NULL
        ALTER TABLE dbo.player_profiles ADD guardian_name nvarchar(120) NULL;
    IF COL_LENGTH('dbo.player_profiles', 'guardian_phone') IS NULL
        ALTER TABLE dbo.player_profiles ADD guardian_phone nvarchar(20) NULL;
    IF COL_LENGTH('dbo.player_profiles', 'guardian_consent_at') IS NULL
        ALTER TABLE dbo.player_profiles ADD guardian_consent_at datetimeoffset NULL;
END;

IF OBJECT_ID(N'dbo.academy_profiles', N'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.academy_profiles', 'training_type') IS NULL
        ALTER TABLE dbo.academy_profiles ADD training_type nvarchar(80) NULL;
    IF OBJECT_ID(N'dbo.academy_age_groups', N'U') IS NULL
        CREATE TABLE dbo.academy_age_groups (
            academy_profile_id bigint NOT NULL,
            age_group nvarchar(40) NULL,
            CONSTRAINT FK_academy_age_groups_profile FOREIGN KEY (academy_profile_id) REFERENCES dbo.academy_profiles(id)
        );
END;

IF OBJECT_ID(N'dbo.club_profiles', N'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.club_profiles', 'website_or_page_url') IS NULL
        ALTER TABLE dbo.club_profiles ADD website_or_page_url nvarchar(500) NULL;
    IF COL_LENGTH('dbo.club_profiles', 'description') IS NULL
        ALTER TABLE dbo.club_profiles ADD description nvarchar(max) NULL;
    IF COL_LENGTH('dbo.club_profiles', 'league_level') IS NULL
        ALTER TABLE dbo.club_profiles ADD league_level nvarchar(80) NULL;
    IF COL_LENGTH('dbo.club_profiles', 'actively_recruiting') IS NULL
        ALTER TABLE dbo.club_profiles ADD actively_recruiting bit NOT NULL CONSTRAINT DF_club_profiles_actively_recruiting DEFAULT (0);
END;
