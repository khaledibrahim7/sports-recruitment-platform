IF OBJECT_ID(N'dbo.academy_profiles', N'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.academy_profiles', 'city_country') IS NULL
        ALTER TABLE dbo.academy_profiles ADD city_country nvarchar(120) NULL;
    IF COL_LENGTH('dbo.academy_profiles', 'contact_phone') IS NULL
        ALTER TABLE dbo.academy_profiles ADD contact_phone nvarchar(20) NULL;
END;

IF OBJECT_ID(N'dbo.club_profiles', N'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.club_profiles', 'contact_phone') IS NULL
        ALTER TABLE dbo.club_profiles ADD contact_phone nvarchar(20) NULL;
END;

IF OBJECT_ID(N'dbo.player_profiles', N'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.player_profiles', 'birth_date') IS NULL
        ALTER TABLE dbo.player_profiles ADD birth_date date NULL;
    IF COL_LENGTH('dbo.player_profiles', 'nationality') IS NULL
        ALTER TABLE dbo.player_profiles ADD nationality nvarchar(80) NULL;
    IF COL_LENGTH('dbo.player_profiles', 'city_country') IS NULL
        ALTER TABLE dbo.player_profiles ADD city_country nvarchar(120) NULL;
    IF COL_LENGTH('dbo.player_profiles', 'current_team') IS NULL
        ALTER TABLE dbo.player_profiles ADD current_team nvarchar(120) NULL;
    IF COL_LENGTH('dbo.player_profiles', 'competition_level') IS NULL
        ALTER TABLE dbo.player_profiles ADD competition_level nvarchar(30) NULL;

    IF OBJECT_ID(N'dbo.player_videos', N'U') IS NULL
        CREATE TABLE dbo.player_videos (
            player_profile_id bigint NOT NULL,
            video_url nvarchar(2048) NULL,
            CONSTRAINT FK_player_videos_profile FOREIGN KEY (player_profile_id) REFERENCES dbo.player_profiles(id)
        );

    IF OBJECT_ID(N'dbo.player_secondary_positions', N'U') IS NULL
        CREATE TABLE dbo.player_secondary_positions (
            player_profile_id bigint NOT NULL,
            secondary_position nvarchar(50) NULL,
            CONSTRAINT FK_player_secondary_positions_profile FOREIGN KEY (player_profile_id) REFERENCES dbo.player_profiles(id)
        );
END;
