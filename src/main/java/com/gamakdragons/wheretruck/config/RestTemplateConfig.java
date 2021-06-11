package com.gamakdragons.wheretruck.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
	
    @Bean
    public RestTemplate createRestTemplate() {

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(5000);
        factory.setConnectTimeout(5000);
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                                            .setMaxConnTotal(100)
                                            .setMaxConnPerRoute(10)
                                            .build();

        factory.setHttpClient(httpClient);

        return new RestTemplate(factory);
    }
}
