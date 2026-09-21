package com.backend.ecommerce_api.service;

import com.backend.ecommerce_api.dto.AuthResponseDto;
import com.backend.ecommerce_api.dto.LoginRequestDto;
import com.backend.ecommerce_api.dto.RegisterRequestDto;
import com.backend.ecommerce_api.entity.Role;
import com.backend.ecommerce_api.entity.User;
import com.backend.ecommerce_api.repository.UserRepository;
import com.backend.ecommerce_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build());
        return createResponse(user.getEmail(), user.getRole());
    }

    public AuthResponseDto login(LoginRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new AuthResponseDto(
                jwtService.generateToken(userDetails), "Bearer", user.getEmail(), user.getRole().name());
    }

    private AuthResponseDto createResponse(String email, Role role) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(email)
                .password("")
                .authorities("ROLE_" + role.name())
                .build();
        return new AuthResponseDto(jwtService.generateToken(userDetails), "Bearer", email, role.name());
    }
}
