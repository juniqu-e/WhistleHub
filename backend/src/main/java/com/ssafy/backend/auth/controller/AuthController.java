package com.ssafy.backend.auth.controller;

import com.ssafy.backend.auth.model.request.RefreshRequestDto;
import com.ssafy.backend.auth.model.request.RegisterRequestDto;
import com.ssafy.backend.auth.model.response.RefreshResponseDto;
import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.ApiResponse;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입을 수행합니다.
     *
     * @param registerRequestDto 회원가입 요청 dto
     * @return 회원가입 성공시 회원id 반환
     */
    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequestDto registerRequestDto) {
        Integer result = authService.register(registerRequestDto);

        return new ApiResponse.builder<Integer>()
                .payload(result)
                .build();
    }

    /**
     * 중복된 아이디가 DB에 있는지 확인합니다.
     *
     * @param loginId 중복 체크할 아이디
     * @return 중복된 아이디가 존재하면 true, 존재하지 않으면 false
     */
    @GetMapping("/duplicated/id")
    public ApiResponse<?> checkDuplicatedId(@PathParam("loginId") String loginId) {
        log.debug("check duplicated id request: {}", loginId);
        boolean result = authService.checkDuplicatedId(loginId);

        return new ApiResponse.builder<Boolean>()
                .payload(result)
                .build();
    }

    /**
     * 중복된 닉네임이 DB에 있는지 확인합니다.
     *
     * @param nickname 중복 체크할 닉네임
     * @return 중복된 닉네임이 존재하면 true, 존재하지 않으면 false
     */
    @GetMapping("/duplicated/nickname")
    public ApiResponse<?> checkDuplicatedNickname(@PathParam("nickname") String nickname) {
        log.debug("check duplicated nickname request: {}", nickname);
        boolean result = authService.checkDuplicatedNickname(nickname);

        return new ApiResponse.builder<Boolean>()
                .payload(result)
                .build();
    }

    /**
     * 중복된 이메일이 DB에 있는지 확인합니다.
     *
     * @param email 중복 체크할 이메일
     * @return 중복된 이메일이 존재하면 true, 존재하지 않으면 false
     */
    @GetMapping("/duplicated/email")
    public ApiResponse<?> checkDuplicatedEmail(@PathParam("email") String email) {
        log.debug("check duplicated email request: {}", email);
        boolean result = authService.checkDuplicatedEmail(email);

        return new ApiResponse.builder<Boolean>()
                .payload(result)
                .build();
    }

    /**
     * refresh 토큰으로 refresh token, access token을 재발급합니다.
     *
     * @param refreshToken refresh token
     * @return 재발급된 refresh token, access token
     */
    @PostMapping("/refresh")
    public ApiResponse<?> refresh(@RequestBody RefreshRequestDto refreshToken) {
        RefreshResponseDto result = authService.refresh(refreshToken);

        return new ApiResponse.builder<RefreshResponseDto>()
                .payload(result)
                .build();
    }

    /**
     * todo: 이메일 인증 요청
     *
     * @param email 이메일
     */
    @GetMapping("/email")
    public ApiResponse<?> verifyEmailRequest(@PathParam("email") String email) {
        authService.verifyEmailRequest(email);

        return new ApiResponse.builder<>()
                .build();
    }
}
