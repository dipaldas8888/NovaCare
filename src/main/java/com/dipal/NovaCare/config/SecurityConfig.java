package com.dipal.NovaCare.config;

import com.dipal.NovaCare.security.FirebaseAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private FirebaseAuthenticationFilter firebaseAuthenticationFilter;

    public SecurityConfig(FirebaseAuthenticationFilter firebaseAuthenticationFilter) {
        this.firebaseAuthenticationFilter = firebaseAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()

                        // Appointments
                        .requestMatchers("/api/appointments").hasAnyRole("ADMIN", "PATIENT")
                        .requestMatchers("/api/appointments/*/accept", "/api/appointments/*/reject").hasRole("DOCTOR")

                        // Patients
                        .requestMatchers("/api/patients/*/credits").hasRole("ADMIN")  // was **/credits -> now */credits
                        .requestMatchers("/api/patients", "/api/patients/**").hasAnyRole("ADMIN", "PATIENT")

                        // Doctors
                        .requestMatchers("/api/doctors/me","/api/doctors/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")

                        // Public
                        .requestMatchers("/api/contact/**").permitAll()

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(firebaseAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
