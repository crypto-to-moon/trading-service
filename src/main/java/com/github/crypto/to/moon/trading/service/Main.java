package com.github.crypto.to.moon.trading.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import static java.lang.Integer.parseInt;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class Main {
    public static void main(String[] args) {

        try {
            SpringApplication.run(Main.class, args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}