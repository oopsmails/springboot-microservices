package com.oopsmails.springboot.sso.jwt.resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

@SpringBootApplication
public class WebApplicationSsoJwtResource {
    @Value("${services.auth}")
    private String authService;

    @Bean
    public FilterRegistrationBean jwtFilter() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new JwtFilter());
        registrationBean.setInitParameters(Collections.singletonMap("services.auth", authService));
        registrationBean.addUrlPatterns("/protected-resource");

        return registrationBean;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebApplicationSsoJwtResource.class, args);
    }
}

