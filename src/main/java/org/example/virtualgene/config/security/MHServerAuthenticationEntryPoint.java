package org.example.virtualgene.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class MHServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    @Value("${homePage}")
    private URI homePage;

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return redirectStrategy.sendRedirect(exchange, homePage);
    }
}
