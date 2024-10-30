package org.example.virtualgene.controller;

import org.example.virtualgene.DTO.AuthenticationDTO;
import org.example.virtualgene.DTO.LoginDTO;
import org.example.virtualgene.DTO.NewAccountDTO;
import org.example.virtualgene.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/security")
public class SecurityController {
    @Autowired
    AccountService accountService;

    @GetMapping("/public")
    public Mono<ResponseEntity<?>> getPublicKey() {
        return accountService.generateKey().map(ResponseEntity::ok);
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<String>> signup(@RequestBody NewAccountDTO newAccountDto) {
        return accountService.createAccount(newAccountDto)
                .map(account -> ResponseEntity.ok().body("success"))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthenticationDTO>> login(@RequestBody LoginDTO loginDTO) {
        return accountService.matchAccount(loginDTO).map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(AuthenticationDTO.builder().feedback(e.getMessage()).build())));
    }
}
