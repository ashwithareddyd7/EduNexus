package com.edunexus.backend.service;

import com.edunexus.backend.dto.AuthResponse;
import com.edunexus.backend.dto.LoginRequest;
import com.edunexus.backend.dto.RegisterRequest;
import com.edunexus.backend.dto.UserResponse;
import com.edunexus.backend.entity.Role;
import com.edunexus.backend.entity.RoleName;
import com.edunexus.backend.entity.User;
import com.edunexus.backend.exception.ConflictException;
import com.edunexus.backend.repository.RoleRepository;
import com.edunexus.backend.repository.UserRepository;
import com.edunexus.backend.security.CustomUserDetails;
import com.edunexus.backend.security.JwtService;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** Public registration always creates a STUDENT. HOD and ADMIN accounts are never self-registered. */
    @Transactional
    public UserResponse registerStudent(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new IllegalStateException("STUDENT role is missing; check role seeding"));

        User user = new User(email, passwordEncoder.encode(request.password()), studentRole); // BCrypt hash

        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            // Two simultaneous registrations with the same email: the unique constraint catches the second.
            throw new ConflictException("An account with this email already exists");
        }

        return new UserResponse(user.getId(), user.getEmail(), studentRole.getName().name());
    }

    public AuthResponse login(LoginRequest request) {
        // Throws AuthenticationException on wrong credentials or a disabled account (handled as 401).
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email().trim().toLowerCase(Locale.ROOT), request.password()));

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);

        return new AuthResponse(token, "Bearer", jwtService.getExpirationMs(),
                principal.getUsername(), principal.getRole());
    }

    public UserResponse currentUser(CustomUserDetails principal) {
        return new UserResponse(principal.getId(), principal.getUsername(), principal.getRole());
    }
}