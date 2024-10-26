package org.example.virtualgene.service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

import java.security.Key;
import java.util.Date;
import java.util.Map;

public class TokenUtils {
    @Getter
    private final Integer expirationTime;
    private final Key signature;

    public TokenUtils(String secret, Integer expirationTime) {
        this.expirationTime = expirationTime;
        this.signature = Keys.hmacShaKeyFor(secret.getBytes());
    }

    private Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(signature).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public String getUserFromToken(String token) {
        return parseToken(token).getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        return parseToken(token).getExpiration();
    }

    public boolean validateToken(String token) {
        try {
            return getExpirationDateFromToken(token).after(new Date());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String refreshToken(String token) {
        Claims allValue = getAllValue(token);
        return generateToken(allValue, allValue.getSubject(), new Date().getTime());
    }

    public <T> T getValueFromToken(String token, String key, Class<T> clazz) {
        return parseToken(token).get(key, clazz);
    }

    public Object getValueFromToken(String token, String key) {
        return parseToken(token).get(key);
    }

    public Claims getAllValue(String token) {
        return parseToken(token);
    }

    public String generateToken(Map<String, Object> claims, String username, boolean isExpiry) {
        Date createdDate = new Date();
        if (isExpiry) {
            Date expired = new Date(createdDate.getTime() + expirationTime * 1000);
            return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date()).setExpiration(expired).signWith(signature).compact();
        }
        return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date()).signWith(signature).compact();
    }

    public String generateToken(Claims claims, String username, long time) {
        Date expired = new Date(time + expirationTime * 1000);
        return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date()).setExpiration(expired).signWith(signature).compact();
    }
}
