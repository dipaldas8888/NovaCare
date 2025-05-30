package com.dipal.NovaCare.repository;


import com.dipal.NovaCare.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorIdAndAppointmentDateTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);

    long countByDoctorIdAndAppointmentDateTimeBetween(Long id, LocalDateTime startOfDay, LocalDateTime endOfDay);

    CharSequence findByDoctorIdAndAppointmentDateTime(Long id, LocalDateTime appointmentDateTime);
}
