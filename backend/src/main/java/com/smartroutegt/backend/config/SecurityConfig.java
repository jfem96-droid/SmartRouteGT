package com.smartroutegt.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                                                .accessDeniedHandler(new AccessDeniedHandlerImpl()))
                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(
                                                                "/api/auth/register",
                                                                "/api/auth/login",
                                                                "/api/status",
                                                                "/error")
                                                .permitAll()

                                                .requestMatchers("/api/users/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/drivers/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/assignments")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/violations")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/buses/**",
                                                                "/api/routes/**",
                                                                "/api/stops/**",
                                                                "/api/route-stops/**",
                                                                "/api/assignments/**",
                                                                "/api/drivers/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/buses/**",
                                                                "/api/routes/**",
                                                                "/api/stops/**",
                                                                "/api/route-stops/**",
                                                                "/api/assignments/**",
                                                                "/api/drivers/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/buses/**",
                                                                "/api/routes/**",
                                                                "/api/stops/**",
                                                                "/api/route-stops/**",
                                                                "/api/assignments/**",
                                                                "/api/drivers/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/violations/*/confirm",
                                                                "/api/violations/*/reject",
                                                                "/api/passenger-alerts/*/review",
                                                                "/api/passenger-alerts/*/resolve",
                                                                "/api/passenger-reports/*/review",
                                                                "/api/passenger-reports/*/resolve",
                                                                "/api/passenger-reports/*/reject")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/monitoring/**",
                                                                "/api/violations/**",
                                                                "/api/passenger-alerts",
                                                                "/api/passenger-alerts/emergencies",
                                                                "/api/passenger-alerts/*",
                                                                "/api/passenger-reports",
                                                                "/api/passenger-reports/*")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/passenger-alerts",
                                                                "/api/passenger-alerts/sos",
                                                                "/api/passenger-reports")
                                                .hasRole("PASSENGER")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/locations")
                                                .hasRole("DRIVER")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/trips/start")
                                                .hasRole("DRIVER")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/trips/*/finish")
                                                .hasRole("DRIVER")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/buses/**",
                                                                "/api/routes/**",
                                                                "/api/stops/**",
                                                                "/api/route-stops/**",
                                                                "/api/locations/**",
                                                                "/api/trips/**",
                                                                "/api/stop-visits/**",
                                                                "/api/assignments/driver/**")
                                                .hasAnyRole("ADMIN", "DRIVER", "PASSENGER")

                                                .requestMatchers(
                                                                "/api/algorithms/**",
                                                                "/api/search/**",
                                                                "/api/bfs-dfs/**",
                                                                "/api/merge-sort/**")
                                                .hasAnyRole("ADMIN", "DRIVER", "PASSENGER")

                                                .anyRequest()
                                                .authenticated())
                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
