package com.electricitybusiness.api.config;

import com.electricitybusiness.api.mapper.IEntityMapper;
import com.electricitybusiness.api.repository.UserRepository;
import com.electricitybusiness.api.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailService userDetailsService;

    private final JwtService jwtService;

    private final JwtAuthFilter jwtAuthFilter;

    private final UserRepository userRepository;

    private final IEntityMapper entityMapper; // Interface, pas l'implémentation

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/auth/login"
/*                                "/api/users/**",
                                "/api/terminals/**",
                                "/api/options/**",
                                "/api/bookings/**",
                                "/api/medias/**",
                                "/api/cars/**"*/

                        ).permitAll()
                        .requestMatchers(OPTIONS, "/**").permitAll()
                        .requestMatchers(POST,
                                "/api/auth/login",
                                "/api/users"
                        ).permitAll()
                        .requestMatchers(
                                "/api/addresses/**",
                                "/api/places/**",
/*
                                "/api/users/**",
*/
                                "/api/terminals/**",
                                "/api/options/**",
                                "/api/bookings/**",
                                "/api/medias/**",
                                "/api/cars/**"

                        ).hasAnyAuthority("USER", "ADMIN")
/*                        .requestMatchers(

                        ).hasAnyAuthority("USER", "ADMIN")*/
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/repairers/**"
                        ).hasRole("ADMIN")


                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        http
                .exceptionHandling(exh -> exh.authenticationEntryPoint(
                (request, response, exception) -> {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
                }));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Lazy
    @Autowired
    public SecurityConfig(
        IEntityMapper entityMapper,
        UserRepository userRepository,
        CustomUserDetailService userDetailsService,
        JwtService jwtService,
        JwtAuthFilter jwtAuthFilter
    ) {
        this.entityMapper = entityMapper;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.jwtAuthFilter = jwtAuthFilter;
    }
}
