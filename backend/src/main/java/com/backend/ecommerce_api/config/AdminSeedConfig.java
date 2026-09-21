package com.backend.ecommerce_api.config;

import com.backend.ecommerce_api.entity.Role;
import com.backend.ecommerce_api.entity.User;
import com.backend.ecommerce_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminSeedConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedAdmin(
            @Value("${app.seed-admin.enabled:false}") boolean enabled,
            @Value("${app.seed-admin.email:}") String email,
            @Value("${app.seed-admin.password:}") String password) {
        return args -> {
            if (!enabled) {
                return;
            }
            if (email.isBlank() || password.length() < 8) {
                throw new IllegalStateException("Admin seed requires an email and a password of at least 8 characters");
            }
            String normalizedEmail = email.trim().toLowerCase();
            User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                    .orElseGet(() -> User.builder().email(normalizedEmail).build());
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(Role.ADMIN);
            userRepository.save(user);
        };
    }
}
