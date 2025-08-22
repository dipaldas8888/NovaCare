package com.dipal.NovaCare.config;

// src/main/java/com/dipal/NovaCare/config/AdminSeeder.java

import com.dipal.NovaCare.model.Role;
import com.dipal.NovaCare.model.User;
import com.dipal.NovaCare.repository.RoleRepository;
import com.dipal.NovaCare.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FirebaseAuth firebaseAuth;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.username:admin}")
    private String adminUsername;

    public AdminSeeder(UserRepository userRepository,
                       RoleRepository roleRepository,
                       FirebaseAuth firebaseAuth) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1) Ensure ROLE_ADMIN exists
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_ADMIN");
                    return roleRepository.save(r);
                });

        // 2) Ensure Firebase user exists
        UserRecord firebaseUser;
        try {
            firebaseUser = firebaseAuth.getUserByEmail(adminEmail);
        } catch (Exception e) {
            UserRecord.CreateRequest req = new UserRecord.CreateRequest()
                    .setEmail(adminEmail)
                    .setEmailVerified(true)
                    .setPassword(adminPassword)
                    .setDisplayName(adminUsername)
                    .setDisabled(false);
            firebaseUser = firebaseAuth.createUser(req);
        }

        // 3) Ensure DB user exists
        if (!userRepository.existsByEmail(adminEmail)) {
            User u = new User();
            u.setEmail(adminEmail);
            // store Firebase UID so you can map tokens -> user rows later
            u.setFirebaseUid(firebaseUser.getUid());
            // Add ROLE_ADMIN to roles set
            u.setRoles(Collections.singleton(adminRole));

            userRepository.save(u);
            System.out.println("Seeded admin in Neon DB: " + adminEmail);
        }
    }
}
