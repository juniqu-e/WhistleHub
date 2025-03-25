package com.ssafy.backend.auth.service;

import com.ssafy.backend.Mail.model.common.EmailMessage;
import com.ssafy.backend.Mail.service.EmailService;
import com.ssafy.backend.auth.model.common.CustomUserDetails;
import com.ssafy.backend.auth.model.request.RefreshRequestDto;
import com.ssafy.backend.auth.model.request.RegisterRequestDto;
import com.ssafy.backend.auth.model.request.ValidateEmailRequestDto;
import com.ssafy.backend.auth.model.response.RefreshResponseDto;
import com.ssafy.backend.common.config.JWTConfig;
import com.ssafy.backend.common.error.exception.*;
import com.ssafy.backend.common.prop.JWTProp;
import com.ssafy.backend.common.prop.MailProp;
import com.ssafy.backend.common.service.RedisService;
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
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final DataCollectingService dataCollectingService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;
    private final EmailService emailService;
    private final RedisService redisService;
    private final MailProp mailProp;
    private final JWTProp jwtProp;

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
        //todo : 이메일 인증 체크

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
        String newAccessToken = jwtUtil.createJwt(claims, jwtProp.getACCESS_TOKEN_EXPIRATION());

        claims.put("refresh", true);
        String newRefreshToken = jwtUtil.createJwt(claims, jwtProp.getREFRESH_TOKEN_EXPIRATION());

        return RefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void validateEmailRequest(String email) {
        // 이미 가입된 이메일인지 확인
        if (checkDuplicatedEmail(email))
            throw new DuplicateEmailException();

        // 이메일 인증 코드 생성
        String code = UUID.randomUUID().toString().substring(0, mailProp.getMAIL_CODE_LENGTH().intValue());

        // 이메일 전송
        // 메일 객체 생성
        EmailMessage emailMessage = EmailMessage.builder()
                .to(email)
                .subject("[WhistleHub] 이메일 인증 코드")
                .message("WhistleHub 이메일 인증 코드입니다 <br /> <strong>" + code + "</strong>")
                .build();

        emailService.sendMail(emailMessage, true); // 내부적으로 메일발송에 실패했을때 예외를 던집니다.
        // redis에 저장
        // key : email, value : code
        redisService.set(email, code, mailProp.getMAIL_CODE_LENGTH().intValue());
    }

    public void validateEmail(ValidateEmailRequestDto validateEmailRequestDto) {
        // code가 없으면 인증 실패 (만료되거나, 이메일이 잘못된 경우)
        Object codeObject = redisService.get(validateEmailRequestDto.getEmail());
        if (codeObject == null)
            throw new InvalidEmailAuthException();

        // code가 일치하지 않으면 인증 실패
        String code = (String) codeObject;
        if (!code.equals(validateEmailRequestDto.getCode()))
            throw new InvalidEmailAuthException();

        //코드와 이메일이 일치하면 인증 성공
        // redis에서 code 삭제
        redisService.delete(validateEmailRequestDto.getEmail());
        // redis에 인증 완료된 이메일 저장
        redisService.setKeyOnly(validateEmailRequestDto.getEmail() + "-validated", mailProp.getMAIL_CODE_EXPIRE_TIME());
    }
}
