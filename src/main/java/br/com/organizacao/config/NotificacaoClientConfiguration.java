package br.com.organizacao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class NotificacaoClientConfiguration {

    @Bean
    RestClient notificacaoRestClient(
            RestClient.Builder builder,
            @Value("${integracao.notificacao.base-url}") String baseUrl) {
        return builder
                .requestFactory(new SimpleClientHttpRequestFactory())
                .baseUrl(baseUrl)
                .build();
    }
}
