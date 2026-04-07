package com.application.authService.service;

import com.application.authService.dto.UserRecord;
import org.springframework.http.ResponseEntity;

public interface IUserService {
    ResponseEntity<?> createUser(UserRecord dto);
    ResponseEntity<?> login(UserRecord dto);
}
