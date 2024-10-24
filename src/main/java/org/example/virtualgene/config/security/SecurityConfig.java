package org.example.virtualgene.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    SecurityContextRepository securityContextRepository;
    @Autowired
    MHLogoutSuccessHandler logoutSuccessHandler;
    @Autowired
    MHServerAuthenticationEntryPoint authenticationEntryPoint;

    @Value("${app.success-url}")
    private String successUrl;
    @Value("${spring.session.max-idle-time}")
    private Integer maxIdleTime;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(authorize -> authorize
                        .pathMatchers("/", "/login**", "/error**", "/assets/**", "/security/**").permitAll()
                        .anyExchange().authenticated()
                )
                .formLogin(formLoginSpec ->
                        formLoginSpec.loginPage("/login")
                                .authenticationManager(authenticationManager)
                                .securityContextRepository(securityContextRepository)
                                .authenticationSuccessHandler(new MHLoginSuccessHandler(successUrl, maxIdleTime)))
                .logout(logoutSpec ->
                        logoutSpec.logoutSuccessHandler(logoutSuccessHandler).logoutUrl("/logout").requiresLogout(ServerWebExchangeMatchers.pathMatchers("/logout")))
                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec.authenticationEntryPoint(authenticationEntryPoint))
                .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable).build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:7001");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
