package com.application.authService.service;

import com.application.authService.dto.UserRecord;
import com.application.authService.entity.User;
import com.application.authService.mapper.UserMapper;
import com.application.authService.repository.UserRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRespository userRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    public ResponseEntity<?> createUser(UserRecord dto) {
        if (userRepository.existsById(dto.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
        User user = User.builder()
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .build();
        var response = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.fromEntityToResponse(response));
    }

    public ResponseEntity<?> login(UserRecord dto) {
        return userRepository.findById(dto.username())
                .filter(user -> passwordEncoder.matches(dto.password(), user.getPassword()))
                .map(user -> ResponseEntity.ok(Map.of("token", jwtService.generateToken(user.getUsername()))))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
