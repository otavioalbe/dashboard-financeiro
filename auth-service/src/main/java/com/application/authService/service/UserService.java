package com.application.authService.service;

import com.application.authService.dto.UserRecord;
import com.application.authService.entity.User;
import com.application.authService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public ResponseEntity<?> createUser(UserRecord dto) {
        if (userRepository.existsById(dto.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User user = User.builder()
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .build();
        var response = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", response.getUsername()));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> login(UserRecord dto) {
        return userRepository.findById(dto.username())
                .filter(user -> passwordEncoder.matches(dto.password(), user.getPassword()))
                .map(user -> ResponseEntity.ok(Map.of("token", jwtService.generateToken(user.getUsername()))))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
