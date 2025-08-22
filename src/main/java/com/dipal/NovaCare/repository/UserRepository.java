package com.dipal.NovaCare.repository;

import com.dipal.NovaCare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByFirebaseUid(String firebaseUid);
    Optional<User> findByEmail(String email);
    Boolean existsByFirebaseUid(String firebaseUid);

    Boolean existsByEmail(String email);
}
