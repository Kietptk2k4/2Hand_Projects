package com.twohands.authservice.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ❌ disable CSRF (vì bạn đang dùng API)
            .csrf(csrf -> csrf.disable())

            // ❌ disable form login
            .formLogin(form -> form.disable())

            // ❌ disable basic auth
            .httpBasic(basic -> basic.disable())

            // 🔥 config route
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/auth/**").permitAll() // ✅ mở register
                    .anyRequest().authenticated()           // 🔒 còn lại cần login
            );

        return http.build();
    }
}