package com.dipal.NovaCare.service.impl;

import com.dipal.NovaCare.model.User;
import com.dipal.NovaCare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepo;

    public User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated");
        }
        String principal = auth.getName(); // set by your Firebase filter to UID or email
        return userRepo.findByFirebaseUid(principal)
                .or(() -> userRepo.findByEmail(principal))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
