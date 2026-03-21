package com.application.authService.mapper;

import com.application.authService.dto.UserRecord;
import com.application.authService.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {

    public User fromRequestToEntity(UserRecord dto) {
        return User.builder()
                .username(dto.username())
                .password(dto.password())
                .build();
    }

    public UserRecord fromEntityToResponse(User user) {
        return UserRecord.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }

}
