package com.application.authService.controller;

import com.application.authService.dto.UserRecord;
import com.application.authService.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserRecord> createUser(@RequestBody UserRecord dto) {
        return userService.createUser(dto);
    }
}
