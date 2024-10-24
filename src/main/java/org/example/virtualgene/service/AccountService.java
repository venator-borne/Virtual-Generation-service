package org.example.virtualgene.service;

import org.example.virtualgene.DTO.NewAccountDto;
import org.example.virtualgene.domain.AccountRepository;
import org.example.virtualgene.domain.DAO.Account;
import org.example.virtualgene.service.utils.RSAUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AccountService {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

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
}
