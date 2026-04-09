package com.capstone.healthcare.appointment.service;

import com.capstone.healthcare.appointment.model.Appointment;

import java.util.List;
import java.util.Set;

public interface IAppointmentService {

    Appointment addAppointment(Appointment appointment);

    Set<Appointment> viewAppointments(String patientName);

    Appointment viewAppointment(int appointmentId);

    Appointment updateAppointment(Appointment appointment);

    List<Appointment> getAppointmentList(int centerId, String test, int status);

    Appointment removeAppointment(Appointment appointment);
}
