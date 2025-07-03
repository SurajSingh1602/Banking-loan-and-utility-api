package com.example.banking_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.banking_system.model.User;

import java.util.Optional;

/**
 * UserRepository interface for performing CRUD operations on User entities.
 * Extends JpaRepository to get standard JPA functionalities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a User by their username.
     * Spring Data JPA will automatically generate the query based on the method name.
     *
     * @param username The username to search for.
     * @return An Optional containing the User if found, otherwise empty.
     */
    Optional<User> findByUsername(String username);
}
