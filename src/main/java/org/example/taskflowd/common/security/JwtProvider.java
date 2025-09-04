package org.example.taskflowd.common.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private static final long JWT_EXPIRATION = 1000 * 60 * 60; // 1시간

    private final JwtUserService jwtUserService;

    @Value("${jwt.secret.key}")
    private String secretKey;


    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey); // yml에서 불러온 키 사용
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT 토큰 생성
    public String createToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 토큰에서 Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        String username = getClaims(token).getSubject();

        AuthUser authUser = jwtUserService.findUserById(Long.parseLong(username));
        return new UsernamePasswordAuthenticationToken(authUser, null, new ArrayList<>());
    }


    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
