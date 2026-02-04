package com.encore.encore.global.config;

import com.encore.encore.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }
    // 닉네임을 반환하는 커스텀 메서드 추가
    public String getNickname() {
        return user.getNickname();
    }

    // 권한 반환 (ROLE_USER 등)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash(); // 엔티티의 비밀번호 필드
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // 우리는 이메일로 로그인하니까!
    }


    // 아래 설정들은 필요에 따라 엔티티의 status 필드와 연결하면 됩니다.
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return "ACTIVE".equals(user.getStatus()); }
}
