package com.example.apiproject.security;

import com.example.apiproject.model.OficinaUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OficinaUserDetails implements UserDetails {
    private Long id;
    private String username;
    private String password;
    private Long oficinaId;
    private Collection<? extends GrantedAuthority> authorities;

    public OficinaUserDetails(Long id, String username, String password, Long oficinaId,
                              Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.oficinaId = oficinaId;
        this.authorities = authorities;
    }

    public static OficinaUserDetails build(OficinaUser oficinaUser) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(oficinaUser.getRole())
        );

        return new OficinaUserDetails(
                oficinaUser.getId(),
                oficinaUser.getUsername(),
                oficinaUser.getPassword(),
                oficinaUser.getOficina().getId(), // Captura o ID da oficina
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public Long getId() {
        return id;
    }

    public Long getOficinaId() {
        return oficinaId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OficinaUserDetails that = (OficinaUserDetails) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
