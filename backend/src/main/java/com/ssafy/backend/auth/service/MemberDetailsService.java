package com.ssafy.backend.auth.service;

import com.ssafy.backend.auth.model.common.CustomUserDetails;
import com.ssafy.backend.common.error.exception.NotFoundMemberException;
import com.ssafy.backend.mysql.entity.Member;
import com.ssafy.backend.mysql.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <pre>회원 정보를 조회하는 서비스</pre>
 * Spring Security에서 사용할 UserDetailsService를 구현한 클래스
 *
 * @see Member
 * @author 허현준
 * @version 1.0
 * @since 2025-03-20
 */

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws NotFoundMemberException {
        Member member = memberRepository.findByLoginId(loginId);
        if (member == null)
            throw new NotFoundMemberException();
        return new CustomUserDetails(member);
    }
}
