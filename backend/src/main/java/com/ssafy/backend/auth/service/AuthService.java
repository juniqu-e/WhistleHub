package com.ssafy.backend.auth.service;

import com.ssafy.backend.mail.model.common.EmailMessage;
import com.ssafy.backend.mail.service.EmailService;
import com.ssafy.backend.auth.model.common.CustomUserDetails;
import com.ssafy.backend.auth.model.common.TagDto;
import com.ssafy.backend.auth.model.request.RefreshRequestDto;
import com.ssafy.backend.auth.model.request.RegisterRequestDto;
import com.ssafy.backend.auth.model.request.ResetPasswordRequestDto;
import com.ssafy.backend.auth.model.request.ValidateEmailRequestDto;
import com.ssafy.backend.auth.model.response.RefreshResponseDto;
import com.ssafy.backend.common.error.exception.*;
import com.ssafy.backend.common.prop.JWTProp;
import com.ssafy.backend.common.prop.MailProp;
import com.ssafy.backend.common.service.RedisService;
import com.ssafy.backend.common.util.JWTUtil;
import com.ssafy.backend.graph.service.DataCollectingService;
import com.ssafy.backend.mysql.entity.Member;
import com.ssafy.backend.mysql.entity.Tag;
import com.ssafy.backend.mysql.repository.MemberRepository;
import com.ssafy.backend.mysql.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.UUID;

/**
 * <pre>
 * 회원 인증 관련 비즈니스 로직을 처리합니다.
 * </pre>
 *
 * @author 허현준
 * @version 1.0
 * @date 2025. 3. 26.
 */


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final DataCollectingService dataCollectingService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final EmailService emailService;
    private final RedisService redisService;
    private final MailProp mailProp;
    private final JWTProp jwtProp;
    private final TagRepository tagRepository;

    /**
     * 현재 요청의 jwt 토큰으로 로그인한 회원 정보를 반환합니다.
     *
     * @return 현재 요청의 회원 정보
     */
    public Member getMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        Member member = memberRepository.findById(customUserDetails.getMember().getId())
                .orElseThrow(() -> {
                    log.warn("해당하는 회원이 존재하지 않습니다.");
                    return new NotFoundMemberException();
                });

        if (!member.getEnabled()) {
            log.warn("탈퇴한 회원입니다.");
            throw new NotFoundMemberException();
        }

        return member;
    }

    /**
     * 회원가입을 수행합니다.
     *
     * @param registerRequestDto 회원가입 요청 dto, 아이디, 비밀번호, 닉네임, 이메일, 생일, 성별을 받습니다.
     * @return 회원가입 성공시 회원id 반환
     */
    @Transactional
    public Integer register(RegisterRequestDto registerRequestDto) {
        String loginId = registerRequestDto.getLoginId();
        String nickname = registerRequestDto.getNickname();
        String email = registerRequestDto.getEmail();
        String password = registerRequestDto.getPassword();
        String birth = registerRequestDto.getBirth();
        Character gender = registerRequestDto.getGender();
        List<Integer> tagIdList = registerRequestDto.getTagList();
        List<Tag> tagList = tagRepository.findAllById(tagIdList);


        // 중복 체크 loginId, nickname, email
        if (checkDuplicatedId(loginId))
            throw new DuplicateIdException();
        if (checkDuplicatedNickname(nickname))
            throw new DuplicateNicknameException();
        if (checkDuplicatedEmail(email))
            throw new DuplicateEmailException();

        //todo : 아이디, 비밀번호, 닉네임, 이메일, 생일, 성별 형식 체크

//        if(!redisService.hasKey(email + "-validated")) // 이메일 검증이 완료되지 않았다면,
//            throw new EmailNotValidatedException();
//
//        redisService.delete(email + "-validated");

        // 새 회원 등록
        Member member = Member.builder()
                .loginId(loginId)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .email(email)
                .enabled(true)
                .birth(birth)
                .gender(gender)
                .build();

        member = memberRepository.save(member);

        // 그래프 DB에도 추가
        dataCollectingService.createMember(member.getId());


        // 태그 추가
        for (Tag tag : tagList) {
            log.info("(member: {}) 가 (tag : {}, {})를 선호합니다.", member.getId(), tag.getId(), tag.getName());
            // todo : 태그 선호 관계 추가
            // dataCollectingService.viewTag(member.getId(), tag.getId(), WeightType.LIKE);
        }

        return member.getId();
    }

    /**
     * 중복된 아이디가 DB에 있는지 확인합니다.
     *
     * @param loginId 중복 체크할 아이디
     * @return 중복된 아이디가 존재하면 true, 존재하지 않으면 false
     */
    public boolean checkDuplicatedId(String loginId) {
        boolean result = memberRepository.existsByLoginId(loginId);
        if (result)
            log.warn("{} : 이미 존재하는 아이디입니다.", loginId);

        return result;
    }

    /**
     * 중복된 닉네임이 DB에 있는지 확인합니다.
     *
     * @param nickname 중복 체크할 닉네임
     * @return 중복된 닉네임이 존재하면 true, 존재하지 않으면 false
     */
    public boolean checkDuplicatedNickname(String nickname) {
        boolean result = memberRepository.existsByNickname(nickname);
        if (result)
            log.warn("{} : 이미 존재하는 닉네임입니다.", nickname);

        return result;
    }

    /**
     * 중복된 이메일이 DB에 있는지 확인합니다.
     *
     * @param email 중복 체크할 이메일
     * @return 중복된 이메일이 존재하면 true, 존재하지 않으면 false
     */
    public boolean checkDuplicatedEmail(String email) {
        boolean result = memberRepository.existsByEmail(email);
        if (result)
            log.warn("{} : 이미 존재하는 이메일입니다.", email);

        return result;
    }

    /**
     * refresh 토큰으로 refresh token, access token을 재발급합니다.
     *
     * @param refreshRequestDto refresh token
     * @return 재발급된 refresh token, access token
     */
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

    /**
     * 이메일 인증 코드를 전송합니다.
     *
     * @param email 이메일
     */
    public void validateEmailRequest(String email) {
        // 이미 가입된 이메일인지 확인
        if (checkDuplicatedEmail(email))
            throw new DuplicateEmailException();

        // 이메일 인증 코드 생성
        String code = createCode();

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
        redisService.set(email, code, mailProp.getMAIL_CODE_EXPIRE_TIME().intValue());
    }

    /**
     * 이메일 인증 코드를 검증합니다.
     *
     * @param validateEmailRequestDto 이메일, 코드
     */
    public void validateEmail(ValidateEmailRequestDto validateEmailRequestDto) {
        // code가 없으면 인증 실패 (만료되거나, 이메일이 잘못된 경우)
        Object codeObject = redisService.get(validateEmailRequestDto.getEmail());
        if (codeObject == null)
            throw new InvalidEmailAuthException();

        if (redisService.hasKey(validateEmailRequestDto.getEmail() + "-validated"))
            throw new AlreadyValidatedEmailException();

        // code가 일치하지 않으면 인증 실패
        String code = (String) codeObject;
        if (!code.equals(validateEmailRequestDto.getCode()))
            throw new InvalidEmailAuthException();

        //코드와 이메일이 일치하면 인증 성공
        // redis에서 code 삭제
        redisService.delete(validateEmailRequestDto.getEmail());
        // redis에 인증 완료된 이메일 저장 -> 중복 인증 방지, 가입 요청시 인증된 이메일인지 확인
        redisService.setKeyOnly(validateEmailRequestDto.getEmail() + "-validated");
    }

    /**
     * 비밀번호를 재설정합니다.
     *
     * @param resetPasswordRequestDto 아이디, 이메일
     */
    @Transactional
    public void resetPassword(ResetPasswordRequestDto resetPasswordRequestDto) {
        Member member = memberRepository.findByLoginId(resetPasswordRequestDto.getLoginId());

        if (member == null)
            throw new NotMatchIdAndEmailException();
        if (!member.getEmail().equals(resetPasswordRequestDto.getEmail()))
            throw new NotMatchIdAndEmailException();

        // 임시 비밀번호 생성
        String tempPassword = createCode();

        // 비밀번호 변경
        member.setPassword(passwordEncoder.encode(tempPassword));
        memberRepository.save(member);

        // 메일 전송
        // 메일 객체 생성
        EmailMessage emailMessage = EmailMessage.builder()
                .to(member.getEmail())
                .subject("[WhistleHub] 임시 비밀번호 발급")
                .message("WhistleHub 임시 비밀번호입니다 <br /> <strong>" + tempPassword + "</strong>")
                .build();

        emailService.sendMail(emailMessage, true); // 내부적으로 메일발송에 실패했을때 예외를 던집니다.
    }

    public List<TagDto> getTagList() {
        List<Tag> tagList = tagRepository.findAll();
        List<TagDto> tagDtoList = new LinkedList<>();
        for (Tag tag : tagList) {
            TagDto tagDto = TagDto.builder()
                    .id(tag.getId())
                    .name(tag.getName())
                    .build();
            tagDtoList.add(tagDto);
        }

        return tagDtoList;
    }

    /**
     * 랜덤 코드 생성
     *
     * @return 랜덤 코드
     */
    private String createCode() {
        return UUID.randomUUID().toString().substring(0, mailProp.getMAIL_CODE_LENGTH().intValue());
    }
}
