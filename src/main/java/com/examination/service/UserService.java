package com.examination.service;

import com.examination.dto.UserDto;
import com.examination.entity.Role;
import com.examination.entity.User;
import com.examination.exception.ResourceNotFoundException;
import com.examination.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRoleOrderByNameAsc(role);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User createUser(UserDto dto, Role role) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email " + dto.getEmail() + " is already in use");
        }

        String rawPassword = dto.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = "password123"; // default fallback password
        }

        User user = new User(
                dto.getName().trim(),
                dto.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(rawPassword.trim()),
                role
        );
        return userRepository.save(user);
    }

    public User updateUser(Long id, UserDto dto) {
        User user = getUserById(id);

        if (userRepository.existsByEmailAndIdNot(dto.getEmail().trim().toLowerCase(), id)) {
            throw new IllegalArgumentException("Email " + dto.getEmail() + " is already in use by another account");
        }

        user.setName(dto.getName().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(Role role, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getUsersByRole(role);
        }
        return userRepository.searchByRoleAndKeyword(role, keyword.trim());
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password does not match");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }
        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);
    }

    public void updateProfile(Long userId, String name, String email) {
        User user = getUserById(userId);
        if (userRepository.existsByEmailAndIdNot(email.trim().toLowerCase(), userId)) {
            throw new IllegalArgumentException("Email is already taken by another account");
        }
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    @Transactional(readOnly = true)
    public List<User> getRecentUsers() {
        return userRepository.findTop5ByOrderByCreatedAtDesc();
    }
}
