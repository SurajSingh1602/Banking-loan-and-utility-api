package com.example.banking_system.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * UserRegistrationDto is a Data Transfer Object used for user registration forms.
 * It carries the data needed to create a new user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {
    private String username;
    private String password;
    // You could add confirmPassword, email, etc. here for a more robust registration form
}
