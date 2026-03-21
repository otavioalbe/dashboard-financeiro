package com.application.authService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="users")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(nullable = false, unique = true)
    private String username;
    @Column
    private String password;
}