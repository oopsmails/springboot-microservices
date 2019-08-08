package com.oopsmails.springboot.session.redis.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class SessionUiApplication {

	
	// disable base64 encoding of the sessionId in the cookie for demostration
//	@Bean
//	public HttpSessionIdResolver httpSessionIdResolver() {
//		CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
//		DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
//		cookieSerializer.setUseBase64Encoding(false);
//		resolver.setCookieSerializer(cookieSerializer);
//	    return resolver; 
//	}
	
	public static void main(String[] args) {
		SpringApplication.run(SessionUiApplication.class, args);
	}
}
