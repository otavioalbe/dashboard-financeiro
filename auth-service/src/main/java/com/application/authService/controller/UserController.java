package com.application.authService.controller;

import com.application.authService.dto.UserRecord;
import com.application.authService.service.IUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRecord dto) {
        return userService.createUser(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserRecord dto) {
        return userService.login(dto);
    }
}
