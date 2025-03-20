package com.ssafy.backend.auth.controller;

import com.ssafy.backend.auth.model.request.RegisterRequestDto;
import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.ApiResponse;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입을 수행합니다.
     *
     * @param registerRequestDto 회원가입 요청 dto
     */
    @PostMapping("/register")
    public ApiResponse<?> register(RegisterRequestDto registerRequestDto) {
        log.debug("register request: {}", registerRequestDto);
        Integer result = authService.register(registerRequestDto);

        return new ApiResponse.builder<Integer>()
                .payload(result)
                .build();
    }

    /**
     * todo: 아이디 중복 체크
     * 중복된 아이디가 DB에 있는지 확인합니다.
     *
     * @param loginId 중복 체크할 아이디
     */
    @GetMapping("/duplicated/id")
    public void checkDuplicatedId(@PathParam("loginId") String loginId) {

    }

    /**
     * todo: 닉네임 중복 체크
     * 중복된 닉네임이 DB에 있는지 확인합니다.
     *
     * @param nickname 중복 체크할 닉네임
     */
    @GetMapping("/duplicated/nickname")
    public void checkDuplicatedNickname(@PathParam("nickname") String nickname) {

    }

    /**
     * todo: 이메일 중복 체크
     * 중복된 이메일이 DB에 있는지 확인합니다.
     *
     * @param email 중복 체크할 이메일
     */
    @GetMapping("/duplicated/email")
    public void checkDuplicatedEmail(@PathParam("email") String email) {

    }

    /**
     * todo: 로그인
     * 로그인을 수행합니다.
     *
     */
    @PostMapping("/login")
    public void login() {

    }
}
