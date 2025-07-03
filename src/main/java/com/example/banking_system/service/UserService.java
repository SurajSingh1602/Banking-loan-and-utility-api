package com.example.banking_system.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.banking_system.model.User;
import com.example.banking_system.model.UserRegistrationDto;
import com.example.banking_system.repository.UserRepository;

import java.util.Optional;

/**
 * UserService provides business logic for user management, including registration.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user.
     *
     * @param registrationDto The DTO containing registration details (username, password).
     * @return The newly registered User entity.
     * @throws IllegalArgumentException if the username already exists.
     */
    public User registerNewUser(UserRegistrationDto registrationDto) {
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + registrationDto.getUsername() + "' already exists.");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRoles("ROLE_USER");

        return userRepository.save(user);
    }

    /**
     * Finds a user by username.
     * @param username The username to search for.
     * @return An Optional containing the User if found, otherwise empty.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Finds a user by ID.
     * @param id The ID to search for.
     * @return An Optional containing the User if found, otherwise empty.
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}