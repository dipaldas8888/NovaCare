package com.dipal.NovaCare.service;


import com.dipal.NovaCare.dto.AppointmentDTO;
import com.dipal.NovaCare.model.Appointment;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {
    Appointment bookAppointment(AppointmentDTO appointmentDTO);
    List<Appointment> getAllAppointments();
    Appointment getAppointmentById(Long id);
    List<Appointment> getAppointmentsByPatient(Long patientId);
    List<Appointment> getAppointmentsByDoctor(Long doctorId);
    List<Appointment> getDoctorAppointmentsBetweenDates(Long doctorId, LocalDateTime start, LocalDateTime end);
    Appointment updateAppointment(Long id, AppointmentDTO appointmentDTO);
    void cancelAppointment(Long id);


        Appointment acceptAppointment(Long id);
    boolean isUserInAppointment(String userEmail, String appointmentId);

        void rejectAppointment(Long id);

}
