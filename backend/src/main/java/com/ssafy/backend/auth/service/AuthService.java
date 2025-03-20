package com.ssafy.backend.auth.service;

import com.ssafy.backend.auth.model.request.RegisterRequestDto;
import com.ssafy.backend.common.error.exception.DuplicateIdException;
import com.ssafy.backend.mysql.entity.Member;
import com.ssafy.backend.mysql.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Integer register(RegisterRequestDto registerRequestDto) {
        String loginId = registerRequestDto.getLoginId();
        String nickname = registerRequestDto.getNickname();
        String email = registerRequestDto.getEmail();
        String password = registerRequestDto.getPassword();
        checkDuplicatedId(loginId);
        checkDuplicatedNickname(nickname);
        checkDuplicatedEmail(email);


        // 새 회원 등록
        Member member = Member.builder()
                .loginId(loginId)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .email(email)
                .enabled(true)
                .firstLogin(true)
                .build();

        member = memberRepository.save(member);

        return member.getId();
    }

    public boolean checkDuplicatedId(String loginId) {
        if(memberRepository.existsByLoginId(loginId)) {
            log.warn("{} : 이미 존재하는 아이디입니다.", loginId);
            throw new DuplicateIdException();
        }
        return false;
    }

    public boolean checkDuplicatedNickname(String nickname) {
        if(memberRepository.existsByNickname(nickname)) {
            log.warn("{} : 이미 존재하는 닉네임입니다.", nickname);
            throw new DuplicateIdException();
        }
        return false;
    }

    public boolean checkDuplicatedEmail(String email) {
        if(memberRepository.existsByEmail(email)) {
            log.warn("{} : 이미 존재하는 이메일입니다.", email);
            throw new DuplicateIdException();
        }
        return false;
    }


}
