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
                                .cors(cors -> {
                                })
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
                                                        "/api/auth/verify-email",
                                                        "/api/auth/resend-code",
                                                        "/api/auth/forgot-password",
                                                        "/api/auth/reset-password",
                                                                "/api/status",
                                                                "/error")
                                                .permitAll()

                                                .requestMatchers(HttpMethod.GET, "/api/passenger-feedback").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/passenger-feedback").hasRole("PASSENGER")
                                                .requestMatchers("/api/passenger/profile/**").hasRole("PASSENGER")

                                                        .requestMatchers("/api/boarding/trip/*/code").hasAnyRole("ADMIN", "DRIVER")
                                                        .requestMatchers("/api/boarding/**").hasRole("PASSENGER")

                                                .requestMatchers("/api/users/**", "/api/admin/permanent-delete/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/drivers/license/*")
                                                .hasAnyRole("ADMIN", "DRIVER")

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
                                                                "/api/violations/*",
                                                                "/api/violations/*/confirm",
                                                                "/api/violations/*/reject",
                                                                "/api/violations/*/cancel",
                                                                "/api/passenger-alerts/*/review",
                                                                "/api/passenger-alerts/*/resolve",
                                                                "/api/passenger-reports/*/review",
                                                                "/api/passenger-reports/*/resolve",
                                                                "/api/passenger-reports/*/reject")
                                                .hasRole("ADMIN")

                                        .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/violations/driver/*")
                                                .hasAnyRole("ADMIN", "DRIVER")

                                                // Viajes activos: lectura necesaria para reportes de pasajeros.
                                                .requestMatchers(HttpMethod.GET, "/api/routes/*/itineraries", "/api/routes/*/journey-options")
                                                .hasAnyRole("ADMIN", "DRIVER", "PASSENGER")

                                                .requestMatchers(HttpMethod.PUT, "/api/routes/*/return-stops")
                                                .hasRole("ADMIN")

                                                .requestMatchers(HttpMethod.GET, "/api/dispatch/driver/*", "/api/dispatch/trip/*")
                                                .hasAnyRole("ADMIN", "DRIVER")

                                                .requestMatchers(HttpMethod.PUT, "/api/trips/*/admin-finish").hasRole("ADMIN")

                                                .requestMatchers(HttpMethod.PUT, "/api/trips/*/next-leg")
                                                .hasRole("DRIVER")

                                                .requestMatchers(HttpMethod.GET, "/api/trips/active")
                                                .hasAnyRole("ADMIN", "PASSENGER")

                                        .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/audit-logs",
                                                                "/api/monitoring/**",
                                                                "/api/violations/**",
                                                                "/api/passenger-alerts",
                                                                "/api/passenger-alerts/emergencies",
                                                                "/api/passenger-alerts/*",
                                                                "/api/passenger-reports",
                                                                "/api/passenger-reports/*")
                                                .hasRole("ADMIN")

                                                .requestMatchers(HttpMethod.POST, "/api/driver-reports").hasRole("DRIVER")
                                                .requestMatchers(HttpMethod.GET, "/api/driver-reports/mine").hasRole("DRIVER")
                                                .requestMatchers(HttpMethod.GET, "/api/driver-reports").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/driver-reports/*/status").hasRole("ADMIN")

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
                                                                "/api/locations/bus/*/latest",
                                                                "/api/trips/*/next-stop")
                                                .hasAnyRole("ADMIN", "DRIVER", "PASSENGER")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/locations/trip/**",
                                                                "/api/trips/driver/**",
                                                                "/api/stop-visits/trip/**",
                                                                "/api/assignments/driver/**")
                                                .hasAnyRole("ADMIN", "DRIVER")

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