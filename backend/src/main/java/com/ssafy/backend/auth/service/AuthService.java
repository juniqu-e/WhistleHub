package com.ssafy.backend.auth.service;

import com.ssafy.backend.auth.model.common.CustomUserDetails;
import com.ssafy.backend.auth.model.request.RefreshRequestDto;
import com.ssafy.backend.auth.model.request.RegisterRequestDto;
import com.ssafy.backend.auth.model.response.RefreshResponseDto;
import com.ssafy.backend.common.config.JWTConfig;
import com.ssafy.backend.common.error.exception.*;
import com.ssafy.backend.common.util.JWTUtil;
import com.ssafy.backend.graph.service.DataCollectingService;
import com.ssafy.backend.mysql.entity.Member;
import com.ssafy.backend.mysql.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final DataCollectingService dataCollectingService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;

    public Member getMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        return memberRepository.findById(customUserDetails.getMember().getId())
                .orElseThrow(() -> {
                    log.warn("해당하는 회원이 존재하지 않습니다.");
                    return new NotFoundMemberException();
                });
    }

    @Transactional
    public Integer register(RegisterRequestDto registerRequestDto) {
        String loginId = registerRequestDto.getLoginId();
        String nickname = registerRequestDto.getNickname();
        String email = registerRequestDto.getEmail();
        String password = registerRequestDto.getPassword();

        // 중복 체크
        if (checkDuplicatedId(loginId))
            throw new DuplicateIdException();
        if (checkDuplicatedNickname(nickname))
            throw new DuplicateNicknameException();
        if (checkDuplicatedEmail(email))
            throw new DuplicateEmailException();

        //todo : 아이디, 비밀번호, 닉네임, 이메일 형식 체크

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

        // 그래프 DB에도 추가
        dataCollectingService.createMember(member.getId());

        return member.getId();
    }

    public boolean checkDuplicatedId(String loginId) {
        boolean result = memberRepository.existsByLoginId(loginId);
        if (result)
            log.warn("{} : 이미 존재하는 아이디입니다.", loginId);

        return result;
    }

    public boolean checkDuplicatedNickname(String nickname) {
        boolean result = memberRepository.existsByNickname(nickname);
        if (result)
            log.warn("{} : 이미 존재하는 닉네임입니다.", nickname);

        return result;
    }

    public boolean checkDuplicatedEmail(String email) {
        boolean result = memberRepository.existsByEmail(email);
        if (result)
            log.warn("{} : 이미 존재하는 이메일입니다.", email);

        return result;
    }

    public RefreshResponseDto refresh(RefreshRequestDto refreshRequestDto) {
        String refreshToken = refreshRequestDto.getRefreshToken();

        // refresh 토큰 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("refresh token is invalid");
            throw new InvalidRefreshTokenException();
        }

        // refresh 토큰 만료 검증
        if (jwtUtil.isExpired(refreshToken)) {
            log.warn("refresh token is expired");
            throw new ExpiredRefreshTokenException();
        }

        // refresh 토큰에서 loginId, id 추출
        String loginId = jwtUtil.getKey(refreshToken, "loginId");
        Integer id = Integer.parseInt(jwtUtil.getKey(refreshToken, "id"));

        //JWT 토큰 재발급
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("loginId", loginId);
        claims.put("id", id);
        String newAccessToken = jwtUtil.createJwt(claims, jwtConfig.getAccessExpiration());

        claims.put("refresh", true);
        String newRefreshToken = jwtUtil.createJwt(claims, jwtConfig.getRefreshExpiration());

        return RefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

}
