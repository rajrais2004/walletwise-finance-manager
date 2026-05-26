package com.walletwise.pfm.services;

import com.walletwise.pfm.dto.request.RegisterRequest;
import com.walletwise.pfm.entities.User;
import com.walletwise.pfm.exception.GlobalExceptionHandler.*;
import com.walletwise.pfm.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user. Throws ConflictException if username already exists.
     */
    public User register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new ConflictException("Username already registered: " + req.getUsername());
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setPhoneNumber(req.getPhoneNumber());
        return userRepository.save(user);
    }

    /**
     * Looks up a user by username (email). Used by security layer.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
