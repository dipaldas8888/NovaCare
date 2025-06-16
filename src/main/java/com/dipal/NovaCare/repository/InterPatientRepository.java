package com.dipal.NovaCare.repository;

import com.dipal.NovaCare.model.InterEnquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterPatientRepository extends JpaRepository<InterEnquiry, Long> {

}
