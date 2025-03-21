package com.ssafy.backend.auth.model.common;

import com.ssafy.backend.mysql.entity.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

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
