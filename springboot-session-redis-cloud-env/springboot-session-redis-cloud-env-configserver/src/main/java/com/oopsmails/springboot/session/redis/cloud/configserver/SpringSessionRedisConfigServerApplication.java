package com.oopsmails.springboot.session.redis.cloud.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigServer
public class SpringSessionRedisConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSessionRedisConfigServerApplication.class, args);
    }
}
