package com.dipal.NovaCare.controller;


import com.dipal.NovaCare.dto.AppointmentDTO;
import com.dipal.NovaCare.model.Appointment;
import com.dipal.NovaCare.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Appointment> bookAppointment(
            @RequestBody AppointmentDTO appointmentDTO) {
        return new ResponseEntity<>(
                appointmentService.bookAppointment(appointmentDTO),
                HttpStatus.CREATED);
    }
    // ... existing

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Appointment> acceptAppointment(@PathVariable Long id) {
        return new ResponseEntity<>(appointmentService.acceptAppointment(id), HttpStatus.OK);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> rejectAppointment(@PathVariable Long id) {
        appointmentService.rejectAppointment(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return new ResponseEntity<>(
                appointmentService.getAllAppointments(),
                HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        return new ResponseEntity<>(
                appointmentService.getAppointmentById(id),
                HttpStatus.OK);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatient(
            @PathVariable Long patientId) {
        return new ResponseEntity<>(
                appointmentService.getAppointmentsByPatient(patientId),
                HttpStatus.OK);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctor(
            @PathVariable Long doctorId) {
        return new ResponseEntity<>(
                appointmentService.getAppointmentsByDoctor(doctorId),
                HttpStatus.OK);
    }

    @GetMapping("/doctor/{doctorId}/availability")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<Appointment>> getDoctorAppointmentsBetweenDates(
            @PathVariable Long doctorId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return new ResponseEntity<>(
                appointmentService.getDoctorAppointmentsBetweenDates(doctorId, start, end),
                HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Appointment> updateAppointment(
            @PathVariable Long id,
            @RequestBody AppointmentDTO appointmentDTO) {
        return new ResponseEntity<>(
                appointmentService.updateAppointment(id, appointmentDTO),
                HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
