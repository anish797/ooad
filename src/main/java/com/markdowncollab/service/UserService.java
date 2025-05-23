package com.markdowncollab.service;

import java.util.List;
import java.util.stream.Collectors;
import com.markdowncollab.dto.UserDTO;
import com.markdowncollab.exception.UserAlreadyExistsException;
import com.markdowncollab.exception.UserNotFoundException;
import com.markdowncollab.model.User;
import com.markdowncollab.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public UserDTO registerUser(String username, String password, String email, String displayName) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username is already taken");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email is already in use");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setDisplayName(displayName);
        
        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }

    public boolean login(String username, String password) {
        try {
            // First, check if user exists
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Attempt authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // If authentication succeeds, set the context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            return true;
        } catch (AuthenticationException e) {
            logger.error("Authentication failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            return false;
        }
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !authentication.getPrincipal().equals("anonymousUser");
    }

    public UserDTO getCurrentUser() {
        if (!isAuthenticated()) {
            return null;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = findByUsername(username);
        return new UserDTO(user);
    }
}