package org.example.virtualgene.service;

import org.example.virtualgene.DTO.AuthenticationDTO;
import org.example.virtualgene.DTO.LoginDTO;
import org.example.virtualgene.DTO.NewAccountDto;
import org.example.virtualgene.domain.AccountRepository;
import org.example.virtualgene.domain.DAO.Account;
import org.example.virtualgene.service.utils.RSAUtils;
import org.example.virtualgene.service.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AccountService {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    TokenUtils tokenUtils;

    @Autowired
    ReactiveRedisTemplate<String, Object> redisTemplate;

    public Mono<Account> createAccount(NewAccountDto newAccount) {
        String roles = "ROLE_ADMIN,ROLE_USER";
        return accountRepository.countByEmail(newAccount.getEmail())
                .flatMap(n -> {
                    if (n == 0) {
                        newAccount.setPassword(RSAUtils.decryptBase64(newAccount.getPassword()));
                        return accountRepository.save(newAccount.convertoAccount(roles, passwordEncoder));
                    }
                    return Mono.error(new RuntimeException("Email has been registered"));
                });
    }

    public Mono<String> generateKey() {
        return Mono.just(RSAUtils.generateBase64PublicKey());
    }

    public Mono<AuthenticationDTO> matchAccount(LoginDTO loginDto) {
        return accountRepository.findByEmail(loginDto.email()).next().flatMap(t -> {
            if (t.getPassword() != null && !t.getPassword().isEmpty() &&
                    passwordEncoder.matches(RSAUtils.decryptBase64(loginDto.password()), t.getPassword())) {
                Map<String, Object> claims = Map.of(
                        "email", t.getEmail(),
                        "name", t.getName(),
                        "roles", t.getRoles(),
                        "id", t.getId()
                );
                String token = tokenUtils.generateToken(claims, t.getEmail(), false);
                var vops = redisTemplate.opsForValue();
                return vops.set(token, t.getId(), Duration.ofSeconds(tokenUtils.getExpirationTime()))
                        .map(a -> AuthenticationDTO.builder().token(token).build());
            }
            return Mono.error(new RuntimeException("Invalid username or password"));
        });
    }
}
