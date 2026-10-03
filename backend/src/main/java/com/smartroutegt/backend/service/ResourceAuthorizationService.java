package com.smartroutegt.backend.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.smartroutegt.backend.entity.User;
import com.smartroutegt.backend.repository.UserRepository;

@Service
public class ResourceAuthorizationService {

    private final UserRepository userRepository;

    public ResourceAuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean canAccessDriver(Long driverId) {
        if (isAdmin()) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        User user = userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElse(null);

        if (user == null
                || user.getRole() != User.Role.DRIVER
                || user.getDriver() == null
                || user.getDriver().getId() == null) {
            return false;
        }

        return user.getDriver().getId().equals(driverId);
    }
}