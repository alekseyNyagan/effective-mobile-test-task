package com.example.bankcards.config;

import com.example.bankcards.security.AppUserDetailsService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Test configuration that provides security setup for testing.
 * This overrides the main SecurityConfig to match the actual security rules for proper authorization testing.
 */
@TestConfiguration
@EnableWebSecurity
public class SecurityTestConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/card/v1/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/card/v1/myCards").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/card/v1/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/card/v1").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/card/v1/changeCardStatus/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/card/v1/changeManyCardStatus").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/card/v1/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/card/v1").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/user/v1").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/user/v1/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/user/v1").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/user/v1/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/user/v1").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/user/v1/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/user/v1").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/card/v1/block-request").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/card/v1/block-request/*/approve").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/card/v1/*/balance").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/transfer/transfer").hasRole("USER")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Don't add JWT filter - Spring Security Test's jwt() handles authentication
                .build();
    }

    @Bean
    @Primary
    public AppUserDetailsService appUserDetailsService() {
        return Mockito.mock(AppUserDetailsService.class);
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
