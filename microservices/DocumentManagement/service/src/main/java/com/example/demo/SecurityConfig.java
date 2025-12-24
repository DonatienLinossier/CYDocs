package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Désactive CSRF pour permettre les POST/PUT depuis React
            .csrf(csrf -> csrf.disable()) 
            
            // 2. Active la configuration CORS par défaut (utilise @CrossOrigin du controller)
            .cors(Customizer.withDefaults()) 

            // 3. Empêche la redirection 302 vers /login. Renvoie 401 à la place.
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )

            // 4. Autorise temporairement TOUT sur /documents pour débloquer ta collègue
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/documents/**").permitAll() 
                .anyRequest().authenticated()
            );

        return http.build();
    }
}