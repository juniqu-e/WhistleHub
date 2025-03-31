package com.ssafy.backend.auth.service;

import com.ssafy.backend.graph.model.entity.type.WeightType;
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

        // 회원가입에 필요한 검증 시작

        // 입력 값 체크 -> 일반적인 에러 반환
        // 아이디, 비밀번호, 닉네임, 이메일, 생일, 성별 형식 체크
        if (!validateLoginIdFormat(loginId)) {
            log.warn("아이디 형식이 잘못되었습니다.");
            throw new InvalidFormattedRequest();
        }
        if (!validateNicknameFormat(nickname)) {
            log.warn("닉네임 형식이 잘못되었습니다.");
            throw new InvalidFormattedRequest();
        }
        if (!validateEmailFormat(email)) {
            log.warn("이메일 형식이 잘못되었습니다.");
            throw new InvalidFormattedRequest();
        }
        if (!validatePasswordFormat(password)) {
            log.warn("비밀번호 형식이 잘못되었습니다.");
            throw new InvalidFormattedRequest();
        }
        if (!validateBirthFormat(birth)) {
            log.warn("생년월일 형식이 잘못되었습니다.");
            throw new InvalidFormattedRequest();
        }
        if (!validateGenderFormat(gender)) {
            log.warn("성별 형식이 잘못되었습니다.");
            throw new InvalidFormattedRequest();
        }

        // DB데이터 체크 -> 조금더 구체적으로 에러 반환
        // 중복 체크 loginId, nickname, email
        if (checkDuplicatedId(loginId))
            throw new DuplicateIdException();
        if (checkDuplicatedNickname(nickname))
            throw new DuplicateNicknameException();
        if (checkDuplicatedEmail(email))
            throw new DuplicateEmailException();

        if (!redisService.hasKey(email + "-validated")) // 이메일 검증이 완료되지 않았다면,
            throw new EmailNotValidatedException();

        redisService.delete(email + "-validated");

        // 검증 종료
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
        // 회원 노드 생성
        dataCollectingService.createMember(member.getId());

        List<Integer> tagNodeIdList = new LinkedList<>();
        for (Tag tag : tagList) {
            tagNodeIdList.add(tag.getId());
        }

        // 태그 노드 연결
        dataCollectingService.viewTags(member.getId(), tagNodeIdList, WeightType.LIKE);

        return member.getId();
    }

    /**
     * 아이디 형식을 검증합니다.
     * - 4-20자
     * - 영어 대소문자, 숫자 허용
     *
     * @param loginId 검증할 아이디
     * @return 형식이 유효하면 true, 아니면 false
     */
    public boolean validateLoginIdFormat(String loginId) {
        if (loginId == null) return false;
        return loginId.matches("^[a-zA-Z0-9]{4,20}$");
    }

    /**
     * 닉네임 형식을 검증합니다.
     * - 2-20자
     * - 한글, 영어 허용
     *
     * @param nickname 검증할 닉네임
     * @return 형식이 유효하면 true, 아니면 false
     */
    public boolean validateNicknameFormat(String nickname) {
        if (nickname == null) return false;
        return nickname.matches("^[a-zA-Z가-힣0-9]{2,20}$");
    }

    /**
     * 이메일 형식을 검증합니다.
     *
     * @param email 검증할 이메일
     * @return 형식이 유효하면 true, 아니면 false
     */
    public boolean validateEmailFormat(String email) {
        if (email == null) return false;
        // 길이 검증 (최소 5자, 최대 100자)
        if (email.length() < 5 || email.length() > 100) return false;

        return email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    }

    /**
     * 비밀번호 형식을 검증합니다.
     * - 8-64자
     * - 영문 소문자, 영문 대문자, 숫자, 특수 문자가 각각 1개 이상 반드시 포함
     *
     * @param password 검증할 비밀번호
     * @return 형식이 유효하면 true, 아니면 false
     */
    public boolean validatePasswordFormat(String password) {
        if (password == null) return false;

        // 각 문자 종류가 반드시 포함되어야 함
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])"
                + "[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]{8,64}$";

        return password.matches(regex);
    }

    /**
     * 생년월일 형식을 검증합니다.
     * - YYYY-MM-DD 형식
     *
     * @param birth 검증할 생년월일
     * @return 형식이 유효하면 true, 아니면 false
     */
    public boolean validateBirthFormat(String birth) {
        if (birth == null) return false;
        // YYYY-MM-DD 형식 검증
        if (!birth.matches("^\\d{4}-\\d{2}-\\d{2}$")) return false;

        try {
            // 실제 날짜 유효성 검증
            java.time.LocalDate.parse(birth);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 성별 형식을 검증합니다.
     *
     * @param gender 검증할 성별
     * @return 형식이 유효하면 true, 아니면 false
     */
    public boolean validateGenderFormat(Character gender) {
        if (gender == null)
            return false;

        return gender == 'M' || gender == 'F';
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
        if (redisService.hasKey(validateEmailRequestDto.getEmail() + "-validated"))
            throw new AlreadyValidatedEmailException();

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

    /**
     * 태그 목록을 가져옵니다.
     *
     * @return 태그 목록
     */
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
