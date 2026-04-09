package com.capstone.healthcare.appointment.repository;

import com.capstone.healthcare.appointment.model.AppointmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAppointmentStatusHistoryRepository extends JpaRepository<AppointmentStatusHistory, Integer> {

    List<AppointmentStatusHistory> findByAppointmentIdOrderByChangedAtAsc(int appointmentId);
}
