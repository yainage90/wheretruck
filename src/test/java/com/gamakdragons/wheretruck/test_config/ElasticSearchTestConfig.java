package com.gamakdragons.wheretruck.test_config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchTestConfig {
	
    @Value("${elasticsearch.username}")
    private String ES_USER;

    @Value("${elasticsearch.password}")
    private String ES_PASSWORD;

    @Bean
    public RestHighLevelClient createRestHighLevelClient() {

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(ES_USER, ES_PASSWORD));

        RestClientBuilder builder = RestClient.builder(
			HttpHost.create(System.getProperty("elasticsearch.address"))
        )
        .setHttpClientConfigCallback((httpClientBuilder) -> {
            return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        });

		System.clearProperty("elasticsearch.address");

        return new RestHighLevelClient(builder);
    }
}
