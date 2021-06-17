package com.gamakdragons.wheretruck.auth.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamakdragons.wheretruck.auth.dto.apple.ApplePublicKeyResponse;

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

@Slf4j
@Component
public class JwtUtil {

    private static String JWT_SECRET;

    private static int EXPIRATION_TIME;

    public static String generateToken(String userId) {

        Date now = new Date();

        return Jwts.builder()
                    .setSubject(userId)
                    .setIssuedAt(now)
                    .setExpiration(new Date(now.getTime() + EXPIRATION_TIME))
                    .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(JWT_SECRET.getBytes()))
                    .compact();
    }

    public static String validate(String token) {

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

    @SuppressWarnings("unchecked")
    public static String parseUserIdFormAppleJwt(String identityToken, ApplePublicKeyResponse applePublicKeyResponse) {

        String headerOfIdentityToken = identityToken.substring(0, identityToken.indexOf("."));
        Map<String, String> header;
        try {
            header = new ObjectMapper().readValue(new String(Base64.getDecoder().decode(headerOfIdentityToken), StandardCharsets.UTF_8), Map.class);
        } catch (JsonMappingException e1) {
            log.error("JsonMappingException. decoded header of identityToken value cannot be mapped.", e1);
            return null;
        } catch (JsonProcessingException e2) {
            log.error("JsonProcessingException. decoded header of identityToken value cannot be processed with json.", e2);
            return null;
        }

        ApplePublicKeyResponse.Key key = applePublicKeyResponse.getMatchedKeyBy(header.get("kid"), header.get("alg"))
			.orElseThrow(() -> new NullPointerException("Failed get public key from apple's id server."));
		
        byte[] nBytes = Base64.getUrlDecoder().decode(key.getN());
        byte[] eBytes = Base64.getUrlDecoder().decode(key.getE());

        BigInteger n = new BigInteger(1, nBytes);
        BigInteger e = new BigInteger(1, eBytes);

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
        KeyFactory keyFactory;

        try {
            keyFactory = KeyFactory.getInstance(key.getKty());
        } catch (NoSuchAlgorithmException ex) {
            log.error("NoSuchAlgorithmException", ex);
            return null;
        }

        PublicKey publicKey;
        try {
            publicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (InvalidKeySpecException ex) {
            log.error("InvalidKeySpecException", ex);
            return null;
        }

        return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(identityToken).getBody().getSubject();
    }


    @Value("${jwt.secret}")
    public void setSecretKey(String value) {
        JWT_SECRET = value;
    }

    @Value("${jwt.token-validity-in-days}")
    public void setExpirationTime(int value) {
        EXPIRATION_TIME = value * 24 * 60 * 60 * 1000;
    }
    
}
