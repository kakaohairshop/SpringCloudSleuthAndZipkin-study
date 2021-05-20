package com.example.config;

import brave.http.HttpTracing;
import com.example.interceptor.SamplingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcTracingConfig implements WebMvcConfigurer {

    private final HttpTracing httpTracing;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SamplingInterceptor(httpTracing));
    }
}
