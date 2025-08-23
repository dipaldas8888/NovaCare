package com.dipal.NovaCare.config;

import com.dipal.NovaCare.model.Role;
import com.dipal.NovaCare.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {
        if (roleRepository.findByName("PATIENT").isEmpty()) {
            Role r = new Role();
            r.setName("PATIENT");
            roleRepository.save(r);
        }
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role r = new Role();
            r.setName("ADMIN");
            roleRepository.save(r);
        }
        if (roleRepository.findByName("DOCTOR").isEmpty()) {
            Role r = new Role();
            r.setName("DOCTOR");
            roleRepository.save(r);
        }
    }
}
