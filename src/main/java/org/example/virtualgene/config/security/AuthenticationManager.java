package org.example.virtualgene.config.security;

import lombok.AllArgsConstructor;
import org.example.virtualgene.domain.AccountRepository;
import org.example.virtualgene.service.utils.RSAUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@AllArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String username = authentication.getName();
        String credentials = authentication.getCredentials().toString();
        return accountRepository.findByEmailAndPasswordNotNull(username).next().flatMap(t -> {
            if (t.getPassword() != null && !t.getPassword().isEmpty() &&
                    passwordEncoder.matches(RSAUtils.decryptBase64(credentials), t.getPassword())) {
                return Mono.just(new UsernamePasswordAuthenticationToken(username, t, List.of(new SimpleGrantedAuthority(t.getRoles()))));
            }
            return Mono.error(new BadCredentialsException("Invalid username or password"));
        });
    }
}
