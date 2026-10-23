package com.sports.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String fullName;
    private final String roleName;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public UserPrincipal(Long id, String email, String fullName, String roleName, String password, Collection<? extends GrantedAuthority> authorities) {
        this(id, email, fullName, roleName, password, authorities, true);
    }

    public UserPrincipal(Long id, String email, String fullName, String roleName, String password, Collection<? extends GrantedAuthority> authorities, boolean enabled) {
        this.id = id;
        this.email = email;
        this.fullName = fullName != null ? fullName : email;
        this.roleName = roleName != null ? roleName : "USER";
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    public static UserPrincipal create(Long id, String email, String password, String roleName) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);
        return new UserPrincipal(
                id,
                email,
                email,
                roleName,
                password,
                Collections.singletonList(authority)
        );
    }

    public static UserPrincipal create(Long id, String email, String fullName, String password, String roleName) {
        return create(id, email, fullName, password, roleName, true);
    }

    public static UserPrincipal create(Long id, String email, String fullName, String password, String roleName, boolean enabled) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);
        return new UserPrincipal(
                id,
                email,
                fullName,
                roleName,
                password,
                Collections.singletonList(authority),
                enabled
        );
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getRoleName() { return roleName; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}
