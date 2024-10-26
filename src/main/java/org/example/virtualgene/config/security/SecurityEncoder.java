package org.example.virtualgene.config.security;

import org.example.virtualgene.service.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

@Configuration
public class SecurityEncoder {
    @Value("${security.password.secret}")
    private CharSequence secret;
    @Value("${security.password.iteration}")
    private int iteration;
    @Value("${security.password.keyLength}")
    private int keyLength;
    @Value("${security.jwt.token.secret}")
    private String jwt;
    @Value("${security.jwt.token.expiration}")
    private int expiration;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Pbkdf2PasswordEncoder(secret, iteration, keyLength, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
    }

    @Bean
    public TokenUtils tokenUtils() {
        return new TokenUtils(jwt, expiration);
    }
}
