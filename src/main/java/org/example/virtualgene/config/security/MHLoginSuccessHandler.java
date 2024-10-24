package org.example.virtualgene.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@Slf4j
public class MHLoginSuccessHandler implements ServerAuthenticationSuccessHandler {
    private final URI location;
    private final Integer maxIdleTime;
    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();
    private final ServerRequestCache requestCache = new WebSessionServerRequestCache();

    public MHLoginSuccessHandler(String location, Integer maxIdleTime) {
        this.location = URI.create(location);
        this.maxIdleTime = maxIdleTime;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        return exchange.getSession()
                .flatMap(webSession -> {
                    webSession.setMaxIdleTime(Duration.ofMinutes(maxIdleTime));
                    return this.requestCache.getRedirectUri(exchange)
                            .defaultIfEmpty(this.location)
                            .flatMap((location) -> exchange.getSession().then(this.redirectStrategy.sendRedirect(exchange, location))
                    );
                });
    }
}