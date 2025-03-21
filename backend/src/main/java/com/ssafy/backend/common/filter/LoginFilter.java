package com.ssafy.backend.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.auth.model.common.CustomUserDetails;
import com.ssafy.backend.auth.model.response.LoginResponseDto;
import com.ssafy.backend.common.config.JWTConfig;
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
    private final JWTConfig jwtConfig;
    private final ObjectMapper objectMapper;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, JWTConfig jwtConfig) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.jwtConfig = jwtConfig;
        this.objectMapper = new ObjectMapper();
        setFilterProcessesUrl("/api/auth/login");
    }

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
        } catch (AuthenticationException e) {
            // 인증 실패 시 401 상태 설정 및 JSON 응답 작성
            try {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("""
                        {
                            "message": "로그인에 실패했습니다."
                        }
                        """);
            } catch (IOException ioException) {
                // 에러 발생 시에도 로그를 최소화
                log.warn("Failed to write authentication error response.");
            }
            // 로그 출력 생략
            return null;
        } catch (IOException e) {
            log.warn("Failed to parse authentication request body");
            throw new RuntimeException("Invalid request");
        }
    }

    //로그인 성공시
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Member member = userDetails.getMember();
//        System.out.println("member = " + member);
        String loginId = member.getLoginId();
        //JWT 토큰 생성
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("loginId", loginId);
        claims.put("id", member.getId());
        String accessToken = jwtUtil.createJwt(claims, jwtConfig.getAccessExpiration());

        claims.put("refresh", true);
        String refreshToken = jwtUtil.createJwt(claims, jwtConfig.getRefreshExpiration());

        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .profileImage(userDetails.getMember().getProfileImage())
                .nickname(userDetails.getMember().getNickname())
                .build();

        response.setStatus(200);
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(loginResponseDto));
    }

    //로그인 실패시
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }
}