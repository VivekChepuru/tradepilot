package com.tradepilot.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    // Dedicated chain for webhook endpoints — no auth filters applied at all.
    // Must be @Order(1) so it is evaluated before the main chain.
    // Covers both GET /webhook (Meta verification) and POST /webhook (inbound messages).
    @Bean
    @Order(1)
    public SecurityFilterChain webhookFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/webhook", "/webhook/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/ws/**",
                                "/app/**",
                                "/topic/**",
                                "/queue/**",
                                "/websocket-test.html",
                                "/websocket-simple-test.html",
                                "/api/users/register",
                                "/api/dashboard/**",
                                "/api/orders/**",
                                "/api/follow-ups/**",
                                "/api/payments/overdue/**",
                                "/api/approvals/**",
                                "/api/price-rules/**",
                                "/api/negotiation-settings/**",
                                "/api/distributors/**",
                                "/error",
                                "/test"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> {});

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
