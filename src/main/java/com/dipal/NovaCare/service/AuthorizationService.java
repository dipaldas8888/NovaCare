package com.dipal.NovaCare.service;

import com.dipal.NovaCare.model.User;
import com.dipal.NovaCare.repository.DoctorRepository;
import com.dipal.NovaCare.repository.PatientRepository;
import com.dipal.NovaCare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authz")
@RequiredArgsConstructor
public class AuthorizationService {
    private final UserRepository userRepo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;

    public boolean canAccessPatient(Long patientId) {
        var me = getMe();
        return patientRepo.findById(patientId)
                .map(p -> p.getUser().getId().equals(me.getId()))
                .orElse(false);
    }

    public boolean canAccessDoctor(Long doctorId) {
        var me = getMe();
        return doctorRepo.findById(doctorId)
                .map(d -> d.getUser().getId().equals(me.getId()))
                .orElse(false);
    }

    private User getMe() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        String principal = auth.getName();
        return userRepo.findByFirebaseUid(principal)
                .or(() -> userRepo.findByEmail(principal))
                .orElse(null);
    }
}
