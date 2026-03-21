package com.application.authService.dto;

import lombok.Builder;

@Builder
public record UserRecord(String username, String password) {
}
