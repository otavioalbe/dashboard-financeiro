package com.application.authService.service;

import com.application.authService.dto.UserRecord;
import com.application.authService.mapper.UserMapper;
import com.application.authService.repository.UserRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRespository userRepository;
    private UserMapper userMapper;

    public ResponseEntity<UserRecord> createUser(UserRecord dto) {
        var response = userRepository.save(userMapper.fromRequestToEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.fromEntityToResponse(response));
    }
}
