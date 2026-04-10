package com.capstone.healthcare.diagnosticcenter.service;

import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;

import java.util.List;

public interface IDiagnosticCenterService {

    List<DiagnosticCenter> getAllDiagnosticCenters();

    DiagnosticCenter addDiagnosticCenter(DiagnosticCenter diagnosticCenter);

    DiagnosticCenter getDiagnosticCenterById(int diagnosticCenterId);

    DiagnosticCenter updateDiagnosticCenter(DiagnosticCenter diagnosticCenter);

    DiagnosticCenter removeDiagnosticCenter(int id);

    DiagnosticCenter getDiagnosticCenter(String centerName);

    List<Appointment> getListOfAppointments(String centerName);

    DiagnosticTest viewTestDetails(int diagnosticCenterId, String testName);

    DiagnosticTest addTest(int diagnosticCenterId, int testId);

    void removeTest(int diagnosticCenterId, int testId);
}
