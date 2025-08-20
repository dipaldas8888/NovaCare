package com.dipal.NovaCare.service.impl;

import com.dipal.NovaCare.dto.AppointmentDTO;
import com.dipal.NovaCare.exception.CustomException;
import com.dipal.NovaCare.model.*;
import com.dipal.NovaCare.repository.AppointmentRepository;
import com.dipal.NovaCare.repository.DoctorRepository;
import com.dipal.NovaCare.repository.PatientRepository;
import com.dipal.NovaCare.service.AppointmentService;
import com.dipal.NovaCare.service.EmailService;
import com.dipal.NovaCare.service.UserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserService userService;

    private final EmailService emailService;

    @Value("${appointment.credit-cost.normal}")
    private double normalCost;

    @Value("${appointment.credit-cost.video}")
    private double videoCost;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  DoctorRepository doctorRepository,
                                  PatientRepository patientRepository,
                                  UserService userService,

                                  EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.userService = userService;

        this.emailService = emailService;
    }

    @Override
    public Appointment bookAppointment(AppointmentDTO appointmentDTO) {
        Doctor doctor = doctorRepository.findById(appointmentDTO.getDoctorId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Doctor not found"));

        User currentUser = userService.getCurrentUser();
        Patient patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Patient not found"));

        // Credit check and deduction (new)
        double cost = "Video".equals(appointmentDTO.getType()) ? videoCost : normalCost;
        if (patient.getCredits() < cost) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Insufficient credits");
        }
        patient.setCredits(patient.getCredits() - cost);
        patientRepository.save(patient);

        // Existing slot checks...
        LocalDateTime startOfDay = appointmentDTO.getAppointmentDateTime().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        long appointmentsCount = appointmentRepository.countByDoctorIdAndAppointmentDateTimeBetween(
                doctor.getId(), startOfDay, endOfDay);

        if (appointmentsCount >= doctor.getMaxPatientsPerDay()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Doctor has reached maximum appointments for the day");
        }

        boolean slotAvailable = appointmentRepository
                .findByDoctorIdAndAppointmentDateTime(doctor.getId(), appointmentDTO.getAppointmentDateTime())
                .isEmpty();

        if (!slotAvailable) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Appointment slot is not available");
        }

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDateTime(appointmentDTO.getAppointmentDateTime());
        appointment.setType(appointmentDTO.getType()); // New
        appointment.setStatus("Video".equals(appointmentDTO.getType()) ? "Pending" : "Scheduled"); // New logic
        appointment.setNotes(appointmentDTO.getNotes());

        return appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Appointment not found with id: " + id));
    }
    @Override
    public boolean isUserInAppointment(String userEmail, String appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(Long.parseLong(appointmentId));
        if (appointmentOpt.isEmpty()) {
            return false;
        }
        Appointment appointment = appointmentOpt.get();

        // Check if userEmail matches either doctor or patient email in appointment
        return appointment.getDoctor().getEmail().equalsIgnoreCase(userEmail)
                || appointment.getPatient().getEmail().equalsIgnoreCase(userEmail);
    }

    @Override
    public Appointment acceptAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        if (!"Pending".equals(appointment.getStatus())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Cannot accept non-pending appointment");
        }
        appointment.setStatus("Accepted");

        if ("Video".equals(appointment.getType())) {
            // Use appointment ID as room name
            String roomName = appointment.getId().toString();

            // You can generate tokens here or generate on demand in your VideoController
            // For example, just save the roomName in appointment for frontend to use
            appointment.setVideoSessionId(roomName);

            // Optionally notify patient or doctor with room info
            emailService.sendVideoLink(appointment.getPatient().getEmail(), roomName, null);
        }

        return appointmentRepository.save(appointment);
    }


    @Override
    public void rejectAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        if (!"Pending".equals(appointment.getStatus())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Cannot reject non-pending appointment");
        }
        appointment.setStatus("Rejected");

        // Refund credits
        double refund = "Video".equals(appointment.getType()) ? videoCost : normalCost;
        Patient patient = appointment.getPatient();
        patient.setCredits(patient.getCredits() + refund);
        patientRepository.save(patient);

        appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<Appointment> getDoctorAppointmentsBetweenDates(Long doctorId, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(doctorId, start, end);
    }

    @Override
    public Appointment updateAppointment(Long id, AppointmentDTO appointmentDTO) {
        Appointment appointment = getAppointmentById(id);

        if (appointmentDTO.getDoctorId() != null) {
            Doctor doctor = doctorRepository.findById(appointmentDTO.getDoctorId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Doctor not found"));
            appointment.setDoctor(doctor);
        }

        if (appointmentDTO.getPatientId() != null) {
            Patient patient = patientRepository.findById(appointmentDTO.getPatientId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Patient not found"));
            appointment.setPatient(patient);
        }

        if (appointmentDTO.getAppointmentDateTime() != null) {
            appointment.setAppointmentDateTime(appointmentDTO.getAppointmentDateTime());
        }

        if (appointmentDTO.getNotes() != null) {
            appointment.setNotes(appointmentDTO.getNotes());
        }

        return appointmentRepository.save(appointment);
    }

    @Override
    public void cancelAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        appointment.setStatus("Cancelled");
        appointmentRepository.save(appointment);
    }
}