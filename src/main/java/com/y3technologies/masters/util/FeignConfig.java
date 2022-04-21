package com.y3technologies.masters.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.y3technologies.masters.filter.SecurityConstant;

import feign.RequestInterceptor;

@Configuration
public class FeignConfig {

    @Bean
    RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getCredentials() != null) {
                requestTemplate.header("Authorization", SecurityConstant.TOKEN_PREFIX + auth.getCredentials().toString());
            }
        };
    }
}
