package org.example.virtualgene.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
public class RSocketConfig {

    @Value("${app.rsocket.host}")
    private String host;

    @Value("${app.rsocket.port}")
    private Integer port;

    @Bean
    public RSocketRequester getRSocketRequester(RSocketStrategies strategies) {
        return RSocketRequester.builder()
                .rsocketConnector(
                        rSocketConnector ->
                                rSocketConnector.reconnect(Retry.fixedDelay(5, Duration.ofSeconds(1)))
                )
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .rsocketStrategies(strategies)
                .tcp(host, port);
    }

}