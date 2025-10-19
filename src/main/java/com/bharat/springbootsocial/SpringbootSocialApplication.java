package com.bharat.springbootsocial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bharat Social Media Application
 * 
 * A comprehensive social media platform built with Spring Boot
 * 
 * @author Bharat
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class SpringbootSocialApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootSocialApplication.class, args);
        System.out.println("🚀 Bharat Social Media Backend Started Successfully!");
    }

}
