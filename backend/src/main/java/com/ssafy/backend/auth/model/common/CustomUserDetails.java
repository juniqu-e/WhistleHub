package com.ssafy.backend.auth.model.common;

import com.ssafy.backend.mysql.entity.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * <pre>CustomUserDetails</pre>
 * Spring Security에서 사용할 UserDetails를 구현한 클래스
 *
 * @see Member
 * @author 허현준
 * @version 1.0
 * @since 2025-03-20
 */

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final Member member;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getLoginId();
    }

    public int getId() {
        return member.getId();
    }

    public Member getMember() {
        return member;
    }

    @Override
    public boolean isEnabled(){
        return member.getEnabled();
    }
}
