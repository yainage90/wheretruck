package com.gamakdragons.wheretruck.auth.service;

import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Value("${jwt.token-validity-in-ms}")
    private int EXPIRATION_TIME;


    public String generateToken(String userId) {

        Date now = new Date();

        return Jwts.builder()
                    .setSubject(userId)
                    .setIssuedAt(now)
                    .setExpiration(new Date(now.getTime() + EXPIRATION_TIME))
                    .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(JWT_SECRET.getBytes()))
                    .compact();
    }

    public String validate(String token) {

        String jwt;
        try {
            jwt = Jwts.parser()
                        .setSigningKey(Base64.getEncoder().encodeToString(JWT_SECRET.getBytes()))
                        .parseClaimsJws(token).getBody().getSubject();

        } catch (SignatureException e) { 
            log.error("Invalid JWT signature", e.getMessage()); 
            throw new JwtException("Invalid JWT signature");
        } catch (MalformedJwtException e) { 
            log.error("Invalid JWT token", e.getMessage()); 
            throw new JwtException("Invalid JWT token");
        } catch (ExpiredJwtException e) { 
            log.error("JWT token is expired", e.getMessage()); 
            throw new JwtException("JWT token is expired");
        } catch (UnsupportedJwtException e) { 
            log.error("JWT token is unsupported", e.getMessage()); 
            throw new JwtException("JWT token is unsupported");
        } catch (IllegalArgumentException e) { 
            log.error("JWT claims string is empty", e.getMessage()); 
            throw new JwtException("JWT claims string is empty");
        }

        if(!StringUtils.hasLength(jwt)) {
            throw new JwtException("Jwt is null or empty");
        }

        return jwt;

    }
}
