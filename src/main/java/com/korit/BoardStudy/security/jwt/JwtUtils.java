package com.korit.BoardStudy.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;


import java.security.Key;
import java.util.Date;

public class JwtUtils {

    private final Key KEY;

    //JWT 비밀키
    public JwtUtils(@Value("${jwt.secret}") String secret) {
        KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    //토큰 만들기 - JWT 액세스 토큰 문자열로 반환
    public String generateAccessToken(String id) {
        return Jwts.builder()
                .subject("AccessToken")
                .id(id)
                .expiration(new Date(new Date().getTime() + (1000L * 60L * 60L * 24L * 30L)))
                .signWith(KEY)
                .compact();
    }

    //로그인 시 토큰 줌 -> 토큰이 Bearer 토큰인지 확인
    public boolean isBearer(String token) {
        if(token == null) {
            return false;
        }
        if (token.startsWith("Bearer ")) {
            return false;
        }
        return true;
    }


    //Bearer 토큰 : - 이 형식에서 Bearer 부분 없애고 토큰만 추출
    public String removeBearer(String token) {
        return token.replaceFirst("Bearer ", "");

    }


    //토큰에서 사용자 정보 가져오기
    public Claims getClaims(String token) {
        JwtParserBuilder jwtParserBuilder = Jwts.parser();
        jwtParserBuilder.setSigningKey(KEY);
        JwtParser jwtParser = jwtParserBuilder.build();
        return jwtParser.parseClaimsJws(token).getBody();
    }

}
