package com.aibert.dosw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        /** Rutas siempre accesibles sin autenticación (Swagger + API + Actuator). */
        private static final String[] PUBLIC_PATHS = {
                        "/api/v1/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs",
                        "/actuator/health"
        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable) // NOSONAR: stateless REST API authenticated via JWT; CSRF not applicable
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(PUBLIC_PATHS).permitAll()
                                                .anyRequest().denyAll())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();

                // Orígenes permitidos: frontend local + otros microservicios internos
                config.setAllowedOriginPatterns(List.of(
                                "http://localhost:[*]", // NOSONAR: local-dev only; HTTPS not available on loopback
                                "https://*.aibert.com",
                                "https://*.azurewebsites.net",
                                "https://*.azurecontainerapps.io",
                                "https://frontend-umber-seven-28.vercel.app"
                ));

                config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

                config.setAllowedHeaders(List.of(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "X-Request-Id"
                ));

                // Exponer X-Request-Id para que el cliente pueda usarlo en trazabilidad
                config.setExposedHeaders(List.of("X-Request-Id"));

                config.setAllowCredentials(true);
                config.setMaxAge(3600L); // pre-flight cache 1h

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }
}
