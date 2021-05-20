package com.example.config;

import brave.http.HttpTracing;
import brave.spring.web.TracingClientHttpRequestInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebClientTracingConfig {

    @Bean
    public RestTemplate restTemplate(HttpTracing tracing) {
        return new RestTemplateBuilder()
                .additionalInterceptors(TracingClientHttpRequestInterceptor.create(tracing)).build();
    }

}
