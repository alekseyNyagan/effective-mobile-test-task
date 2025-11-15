package com.example.bankcards.config;

import com.example.bankcards.security.AppUserDetailsService;
import com.example.bankcards.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class  SecurityConfig {
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String USER_ROLE = "USER";
    private final JwtAuthFilter jwtAuthFilter;
    private final AppUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, AppUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(AppUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider(userDetailsService, passwordEncoder()))
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/card/v1/all").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.GET, "/card/v1/myCards").hasRole(USER_ROLE)
                        .requestMatchers(HttpMethod.GET, "/card/v1/*").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.POST, "/card/v1").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PATCH, "/card/v1/changeCardStatus/*").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PATCH, "/card/v1/changeManyCardStatus").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/card/v1/*").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/card/v1").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.GET, "/user/v1").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.GET, "/user/v1/*").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.POST, "/user/v1").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PATCH, "/user/v1/*").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PATCH, "/user/v1").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/user/v1/*").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/user/v1").hasRole(ADMIN_ROLE)
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
