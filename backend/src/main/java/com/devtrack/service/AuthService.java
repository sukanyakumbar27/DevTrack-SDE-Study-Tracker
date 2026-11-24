package com.devtrack.service;

import com.devtrack.dto.AuthDtos.*;
import com.devtrack.entity.User;
import com.devtrack.exception.ConflictException;
import com.devtrack.exception.InvalidCredentialsException;
import com.devtrack.repository.UserRepository;
import com.devtrack.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final DemoDataService demoData;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt, DemoDataService demoData) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.demoData = demoData;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(req.password()));
        users.save(user);

        demoData.seedJavaTopics(user);
        if (req.loadSampleData() == null || req.loadSampleData()) {
            demoData.seedSampleData(user);
        }
        return new AuthResponse(jwt.generateToken(email), user.getName(), email);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        User user = users.findByEmail(email)
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        return new AuthResponse(jwt.generateToken(email), user.getName(), email);
    }
}
