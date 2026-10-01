package com.darshan.auth.repository;

import com.darshan.auth.entity.Status;
import com.darshan.auth.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User saveUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("secret");
        user.setStatus(Status.ACTIVE);
        return userRepository.save(user);
    }

    @Test
    @DisplayName("existsByEmail returns true when email is present")
    void existsByEmailReturnsTrueWhenPresent() {
        saveUser("John Doe", "john@example.com");

        assertThat(userRepository.existsByEmail("john@example.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail returns false when email is absent")
    void existsByEmailReturnsFalseWhenAbsent() {
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    @DisplayName("findByEmail returns the user when found")
    void findByEmailReturnsUserWhenFound() {
        saveUser("John Doe", "john@example.com");

        Optional<User> result = userRepository.findByEmail("john@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("findByEmail returns empty when not found")
    void findByEmailReturnsEmptyWhenNotFound() {
        Optional<User> result = userRepository.findByEmail("missing@example.com");

        assertThat(result).isEmpty();
    }
}
