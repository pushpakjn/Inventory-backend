package com.supash.inventory.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String role; // ROLE_USER or ROLE_ADMIN
    
    
}
