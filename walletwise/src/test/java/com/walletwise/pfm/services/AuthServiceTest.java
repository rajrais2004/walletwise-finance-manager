package com.walletwise.pfm.services;

import com.walletwise.pfm.dto.request.RegisterRequest;
import com.walletwise.pfm.entities.User;
import com.walletwise.pfm.exception.GlobalExceptionHandler.ConflictException;
import com.walletwise.pfm.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AuthService authService;

    @Test
    void register_success() {
        when(userRepository.existsByUsername("new@user.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        User saved = new User();
        saved.setUsername("new@user.com");
        when(userRepository.save(any())).thenReturn(saved);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("new@user.com");
        req.setPassword("pass123");
        req.setFullName("New User");
        req.setPhoneNumber("+911234567890");

        User result = authService.register(req);
        assertThat(result.getUsername()).isEqualTo("new@user.com");
        verify(passwordEncoder).encode("pass123");
    }

    @Test
    void register_duplicateThrowsConflict() {
        when(userRepository.existsByUsername("dup@user.com")).thenReturn(true);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("dup@user.com");
        req.setPassword("abc123");
        req.setFullName("Dup");
        req.setPhoneNumber("+911111111111");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already registered");
    }
}
