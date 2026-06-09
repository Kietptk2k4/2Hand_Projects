package com.twohands.commerce_service.config;

import com.twohands.commerce_service.security.RestAuthenticationEntryPoint;
import com.twohands.commerce_service.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/commerce/api/v1/payments/webhooks/**").permitAll()
                        .requestMatchers("/commerce/api/v1/shipments/webhooks/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/commerce/api/v1/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/commerce/api/v1/categories/*/products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/commerce/api/v1/products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/commerce/api/v1/products/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/commerce/api/v1/products/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/commerce/api/v1/products/*/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/commerce/api/v1/shops/*/products").permitAll()
                        .requestMatchers("/commerce/api/v1/internal/moderation/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
