package com.dipal.NovaCare.service.impl;


import com.dipal.NovaCare.dto.AppointmentDTO;
import com.dipal.NovaCare.exception.CustomException;
import com.dipal.NovaCare.model.Appointment;
import com.dipal.NovaCare.model.Doctor;
import com.dipal.NovaCare.model.Patient;
import com.dipal.NovaCare.model.User;
import com.dipal.NovaCare.repository.AppointmentRepository;
import com.dipal.NovaCare.repository.DoctorRepository;
import com.dipal.NovaCare.repository.PatientRepository;
import com.dipal.NovaCare.service.AppointmentService;
import com.dipal.NovaCare.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserService userService;


    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  DoctorRepository doctorRepository,
                                  PatientRepository patientRepository,
                                  UserService userService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.userService = userService;
    }

    @Override
    public Appointment bookAppointment(AppointmentDTO appointmentDTO) {
        Doctor doctor = doctorRepository.findById(appointmentDTO.getDoctorId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Doctor not found"));

        User currentUser = userService.getCurrentUser();
        Patient patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Patient not found"));

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
        appointment.setStatus("Scheduled");
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
