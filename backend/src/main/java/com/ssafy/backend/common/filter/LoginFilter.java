package com.ssafy.backend.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.auth.model.common.CustomUserDetails;
import com.ssafy.backend.auth.model.response.LoginResponseDto;
import com.ssafy.backend.common.FilterApiResponse;
import com.ssafy.backend.common.error.ResponseType;
import com.ssafy.backend.common.prop.JWTProp;
import com.ssafy.backend.common.util.JWTUtil;
import com.ssafy.backend.mysql.entity.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final JWTProp jwtProp;
    private final ObjectMapper objectMapper;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, JWTProp jwtProp) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.jwtProp = jwtProp;
        this.objectMapper = new ObjectMapper();

        // 로그인 요청 URL 설정
        setFilterProcessesUrl("/api/auth/login");
    }

    // 로그인 시도 로직
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            Map<String, String> jsonRequest = objectMapper.readValue(request.getInputStream(), Map.class);

            //클라이언트 요청에서 username, password 추출
            String loginId = jsonRequest.get("loginId");
            String password = jsonRequest.get("password");

            //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginId, password, null);

            //token에 담은 검증을 위한 AuthenticationManager로 전달
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            log.warn("Failed to parse authentication request body : attemptAuthentication", e);
            throw new RuntimeException("Invalid request");
        }
    }

    //로그인 성공시
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Member member = userDetails.getMember();

        String loginId = member.getLoginId();
        //JWT 토큰 생성
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("loginId", loginId);
        claims.put("id", member.getId());
        String accessToken = jwtUtil.createJwt(claims, jwtProp.getACCESS_TOKEN_EXPIRATION());

        claims.put("refresh", true);
        String refreshToken = jwtUtil.createJwt(claims, jwtProp.getREFRESH_TOKEN_EXPIRATION());

        // 로그인 응답 객체 생성
        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .profileImage(userDetails.getMember().getProfileImage())
                .nickname(userDetails.getMember().getNickname())
                .build();

        ResponseType responseType = ResponseType.SUCCESS;

        FilterApiResponse<LoginResponseDto> apiResponse = FilterApiResponse.<LoginResponseDto>builder()
                .payload(loginResponseDto)
                .build()
                .setResponseType(responseType);

        // 응답 반환
        response.setStatus(responseType.getStatus().value());
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        } catch (IOException e) {
            log.warn("Failed to parse authentication request body : successfulAuthentication", e);
            throw new RuntimeException(e);
        }
    }

    //로그인 실패시
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        // 로그인 실패 응답 객체 생성
        ResponseType responseType = ResponseType.INVALID_CREDENTIALS;
        FilterApiResponse<?> apiResponse = FilterApiResponse.builder().build().setResponseType(responseType);

        // 응답 반환
        response.setStatus(responseType.getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        } catch (IOException e) {
            log.warn("Failed to parse authentication request body : unsuccessfulAuthentication", e);
            throw new RuntimeException(e);
        }
    }
}