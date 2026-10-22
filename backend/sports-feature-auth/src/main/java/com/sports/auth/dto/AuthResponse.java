package com.sports.auth.dto;

import com.sports.auth.domain.Role;

public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String fullName;
    private Role role;
    private Object profile;

    public AuthResponse(String accessToken, Long userId, String email, String fullName, Role role) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public Object getProfile() { return profile; }
    public void setProfile(Object profile) { this.profile = profile; }
}
