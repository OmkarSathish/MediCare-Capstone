package com.capstone.healthcare.appointment.service;

import com.capstone.healthcare.appointment.model.Appointment;

import java.util.List;

public interface IAppointmentService {

    Appointment addAppointment(Appointment appointment);

    List<Appointment> viewAppointments(String patientName);

    Appointment viewAppointment(int appointmentId);

    Appointment updateAppointment(Appointment appointment);

    List<Appointment> getAppointmentList(int centerId, String test, int status);

    Appointment removeAppointment(Appointment appointment);
}
