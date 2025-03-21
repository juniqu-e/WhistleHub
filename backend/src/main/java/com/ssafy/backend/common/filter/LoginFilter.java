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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, JWTConfig jwtConfig) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.jwtConfig = jwtConfig;

        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //클라이언트 요청에서 username, password 추출
        String loginId = request.getParameter("loginId");
        String password = request.getParameter("password");

        //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginId, password, null);

        //token에 담은 검증을 위한 AuthenticationManager로 전달
        return authenticationManager.authenticate(authToken);
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

        ObjectMapper objectMapper = new ObjectMapper();

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