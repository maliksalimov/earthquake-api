package com.malik.earthquakeapi.service;

import com.malik.earthquakeapi.dto.AuthResponse;
import com.malik.earthquakeapi.dto.LoginRequest;
import com.malik.earthquakeapi.dto.RegisterRequest;
import com.malik.earthquakeapi.entity.Role;
import com.malik.earthquakeapi.entity.User;
import com.malik.earthquakeapi.exception.DuplicateResourceException;
import com.malik.earthquakeapi.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        System.out.println("Username: " + request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        try {
            User savedUser = userRepository.save(user);
            System.out.println("User saved: " + savedUser);

            String jwtToken = jwtService.generateToken(savedUser);
            System.out.println("JWT Token: " + jwtToken);

            return AuthResponse.builder()
                    .token(jwtToken)
                    .username(savedUser.getUsername())
                    .role(savedUser.getRole().name())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        ));

        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
