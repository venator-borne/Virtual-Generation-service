package org.example.virtualgene.config.security;

import lombok.AllArgsConstructor;
import org.example.virtualgene.domain.AccountRepository;
import org.example.virtualgene.service.utils.RSAUtils;
import org.example.virtualgene.service.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {
    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    ReactiveRedisTemplate<String, Object> redisTemplate;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        return redisTemplate.opsForValue().get(authToken)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid token")))
                .map(id -> {
                    UUID account_id = UUID.fromString(id.toString());
                    String roles = tokenUtils.getValueFromToken(authToken, "roles", String.class);
                    return new UsernamePasswordAuthenticationToken(account_id, authToken,
                            Stream.of(roles).map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
                });
    }

}
