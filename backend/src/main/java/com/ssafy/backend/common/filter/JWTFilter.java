package com.ssafy.backend.common.filter;

import com.ssafy.backend.auth.model.common.CustomUserDetails;
import com.ssafy.backend.common.util.JWTUtil;
import com.ssafy.backend.mysql.entity.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 요청 URL 확인
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/")) {
            // /api/auth/** 경로는 JWT 검증 없이 필터 체인으로 전달
            filterChain.doFilter(request, response);
            return;
        }


        //request에서 Authorization 헤더를 찾음
        String authorization= request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("authorization null or not start with Bearer");
            filterChain.doFilter(request, response);

            return;
        }
        //Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {
            log.warn("token is expired");
            filterChain.doFilter(request, response);

            return;
        }

        //토큰에서 username과 role 획득
        String loginId = jwtUtil.getKey(token,"loginId");
        Integer id = Integer.parseInt(jwtUtil.getKey(token,"id"));

        //userEntity를 생성하여 값 set
        Member member = Member.builder()
                .loginId(loginId)
                .id(id)
                .build();


        //UserDetails에 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(member);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, null);
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}