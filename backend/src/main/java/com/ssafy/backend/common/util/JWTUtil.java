package com.ssafy.backend.common.util;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JWTUtil {

    private final SecretKey secretKey;

    public String getKey(String token, String key) {
        Object value = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)
                .getPayload().get(key, Object.class);
        return value != null ? value.toString() : null;
    }

    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String createJwt(Map<String, Object> claim, Long expiredMs) {
        return Jwts.builder()
                .claims(claim)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}