package com.capstone.healthcare.diagnosticcenter.service.impl;

import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.appointment.repository.IAppointmentRepository;
import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnosticcenter.repository.IDiagnosticCenterRepository;
import com.capstone.healthcare.diagnosticcenter.service.IDiagnosticCenterService;
import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;
import com.capstone.healthcare.diagnostictest.repository.IDiagnosticTestRepository;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiagnosticCenterServiceImpl implements IDiagnosticCenterService {

    private final IDiagnosticCenterRepository centerRepository;
    private final IDiagnosticTestRepository testRepository;
    private final IAppointmentRepository appointmentRepository;

    public DiagnosticCenterServiceImpl(
            IDiagnosticCenterRepository centerRepository,
            IDiagnosticTestRepository testRepository,
            @Lazy IAppointmentRepository appointmentRepository) {
        this.centerRepository = centerRepository;
        this.testRepository = testRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public List<DiagnosticCenter> getAllDiagnosticCenters() {
        return centerRepository.findAll();
    }

    @Override
    @Transactional
    public DiagnosticCenter addDiagnosticCenter(DiagnosticCenter diagnosticCenter) {
        diagnosticCenter.setStatus("ACTIVE");
        return centerRepository.save(diagnosticCenter);
    }

    @Override
    public DiagnosticCenter getDiagnosticCenterById(int diagnosticCenterId) {
        return centerRepository.findById(diagnosticCenterId)
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticCenter", "id", diagnosticCenterId));
    }

    @Override
    @Transactional
    public DiagnosticCenter updateDiagnosticCenter(DiagnosticCenter diagnosticCenter) {
        DiagnosticCenter existing = centerRepository.findById(diagnosticCenter.getId())
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticCenter", "id", diagnosticCenter.getId()));
        existing.setName(diagnosticCenter.getName());
        existing.setContactNo(diagnosticCenter.getContactNo());
        existing.setAddress(diagnosticCenter.getAddress());
        existing.setContactEmail(diagnosticCenter.getContactEmail());
        existing.setServicesOffered(diagnosticCenter.getServicesOffered());
        return centerRepository.save(existing);
    }

    @Override
    @Transactional
    public DiagnosticCenter removeDiagnosticCenter(int id) {
        DiagnosticCenter center = centerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticCenter", "id", id));
        center.setStatus("INACTIVE");
        return centerRepository.save(center);
    }

    @Override
    public DiagnosticCenter getDiagnosticCenter(String centerName) {
        return centerRepository.findByName(centerName)
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticCenter", "name", centerName));
    }

    @Override
    public List<Appointment> getListOfAppointments(String centerName) {
        return appointmentRepository.findByCenterName(centerName);
    }

    @Override
    public DiagnosticTest viewTestDetails(int diagnosticCenterId, String testName) {
        DiagnosticCenter center = centerRepository.findCenterWithTest(diagnosticCenterId, testName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Test '" + testName + "' not offered at center id=" + diagnosticCenterId));
        return center.getTests().stream()
                .filter(t -> t.getTestName().equals(testName))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticTest", "testName", testName));
    }

    @Override
    @Transactional
    public DiagnosticTest addTest(int diagnosticCenterId, int testId) {
        DiagnosticCenter center = getDiagnosticCenterById(diagnosticCenterId);
        DiagnosticTest test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticTest", "id", testId));
        center.getTests().add(test);
        centerRepository.save(center);
        return test;
    }

    @Override
    @Transactional
    public void removeTest(int diagnosticCenterId, int testId) {
        DiagnosticCenter center = getDiagnosticCenterById(diagnosticCenterId);
        DiagnosticTest test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticTest", "id", testId));
        center.getTests().remove(test);
        centerRepository.save(center);
    }
}
