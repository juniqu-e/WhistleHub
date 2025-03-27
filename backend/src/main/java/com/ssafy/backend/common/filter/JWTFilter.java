package com.ssafy.backend.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.auth.model.common.CustomUserDetails;
import com.ssafy.backend.common.FilterApiResponse;
import com.ssafy.backend.common.error.ResponseType;
import com.ssafy.backend.common.util.JWTUtil;
import com.ssafy.backend.mysql.entity.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * <pre>JWT 토큰 검증 필터</pre>
 * JWT 토큰을 검증하는 필터
 *
 * @author 허현준
 * @version 1.0
 * @see JWTUtil
 * @see FilterApiResponse
 * @see Member
 * @since 2025-03-20
 */

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("authorization null or not start with Bearer");

            //응답 객체 생성
            setFilterResponse(response, ResponseType.INVALID_ACCESS_TOKEN);

            return;
        }
        //Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        // 토큰 검증
        if (!jwtUtil.validateToken(token)) {
            log.warn("token is invalid");

            //응답 객체 생성
            setFilterResponse(response, ResponseType.INVALID_ACCESS_TOKEN);

            return;
        }

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {
            log.warn("token is expired");

            //응답 객체 생성
            setFilterResponse(response, ResponseType.EXPIRED_ACCESS_TOKEN);

            return;
        }

        //토큰에서 username과 role 획득
        String loginId = jwtUtil.getKey(token, "loginId");
        Integer id = Integer.parseInt(jwtUtil.getKey(token, "id"));

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

    private void setFilterResponse(HttpServletResponse response, ResponseType responseType) {
        FilterApiResponse<?> apiResponse = FilterApiResponse.builder().build().setResponseType(responseType);

        //응답 객체를 JSON 형태로 변환하여 응답
        response.setStatus(responseType.getStatus().value());
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        } catch (IOException e) {
            log.warn("Failed to parse authentication request body : setFilterResponse", e);
            throw new RuntimeException(e);
        }
    }
}