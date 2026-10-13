-- SQL Server Database Creation Script for Sports Recruitment Platform

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'SportsPlatformDB')
BEGIN
    CREATE DATABASE SportsPlatformDB;
END
GO

USE SportsPlatformDB;
GO

-- Users Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[users]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[users] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [email] VARCHAR(150) NOT NULL UNIQUE,
        [password_hash] VARCHAR(255) NOT NULL,
        [role] VARCHAR(20) NOT NULL,
        [full_name] NVARCHAR(150) NOT NULL,
        [created_at] DATETIME2 NOT NULL DEFAULT GETDATE(),
        [updated_at] DATETIME2 NULL
    );
END
GO

-- Player Profiles Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[player_profiles]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[player_profiles] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [user_id] BIGINT NOT NULL UNIQUE FOREIGN KEY REFERENCES [users]([id]) ON DELETE CASCADE,
        [full_name] NVARCHAR(100) NOT NULL,
        [age] INT NOT NULL,
        [position] NVARCHAR(50) NOT NULL,
        [video_url] VARCHAR(500) NULL,
        [bio] NVARCHAR(MAX) NULL,
        [phone] VARCHAR(20) NULL,
        [created_at] DATETIME2 NOT NULL DEFAULT GETDATE(),
        [updated_at] DATETIME2 NULL
    );
END
GO

-- Academy Profiles Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[academy_profiles]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[academy_profiles] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [user_id] BIGINT NOT NULL UNIQUE FOREIGN KEY REFERENCES [users]([id]) ON DELETE CASCADE,
        [academy_name] NVARCHAR(150) NOT NULL,
        [website_or_page_url] VARCHAR(500) NULL,
        [description] NVARCHAR(MAX) NULL,
        [logo_url] VARCHAR(500) NULL,
        [created_at] DATETIME2 NOT NULL DEFAULT GETDATE(),
        [updated_at] DATETIME2 NULL
    );
END
GO

-- Club Profiles Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[club_profiles]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[club_profiles] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [user_id] BIGINT NOT NULL UNIQUE FOREIGN KEY REFERENCES [users]([id]) ON DELETE CASCADE,
        [club_name] NVARCHAR(150) NOT NULL,
        [logo_url] VARCHAR(500) NULL,
        [city_country] NVARCHAR(100) NULL,
        [created_at] DATETIME2 NOT NULL DEFAULT GETDATE(),
        [updated_at] DATETIME2 NULL
    );
END
GO
