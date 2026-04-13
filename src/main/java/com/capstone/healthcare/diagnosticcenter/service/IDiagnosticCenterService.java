package com.capstone.healthcare.diagnosticcenter.service;

import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.diagnosticcenter.model.CenterTestOffering;
import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;

import java.util.List;

public interface IDiagnosticCenterService {

    List<DiagnosticCenter> getAllDiagnosticCenters();

    DiagnosticCenter addDiagnosticCenter(DiagnosticCenter diagnosticCenter);

    DiagnosticCenter getDiagnosticCenterById(int diagnosticCenterId);

    DiagnosticCenter updateDiagnosticCenter(DiagnosticCenter diagnosticCenter);

    DiagnosticCenter removeDiagnosticCenter(int id);

    DiagnosticCenter getDiagnosticCenter(String centerName);

    List<Appointment> getListOfAppointments(String centerName);

    CenterTestOffering viewTestOffering(int diagnosticCenterId, String testName);

    CenterTestOffering addTest(int diagnosticCenterId, int testId, double price);

    void removeTest(int diagnosticCenterId, int testId);
}
