package org.example.virtualgene.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class MHLogoutSuccessHandler implements ServerLogoutSuccessHandler {

    @Value("${homePage}")
    private String homePage;

    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
        ServerWebExchange ex = exchange.getExchange();
        return ex.getSession()
                .flatMap(session -> {
                    var redirectServerLogoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
                    redirectServerLogoutSuccessHandler.setLogoutSuccessUrl(URI.create(homePage));
                    return redirectServerLogoutSuccessHandler.onLogoutSuccess(exchange, authentication);
                });
    }
}
