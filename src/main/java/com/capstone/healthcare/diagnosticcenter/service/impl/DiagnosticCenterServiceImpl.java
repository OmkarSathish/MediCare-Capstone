package com.capstone.healthcare.diagnosticcenter.service.impl;

import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.appointment.repository.IAppointmentRepository;
import com.capstone.healthcare.diagnosticcenter.model.CenterTestOffering;
import com.capstone.healthcare.diagnosticcenter.model.CenterTestOfferingKey;
import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnosticcenter.repository.ICenterTestOfferingRepository;
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
    private final ICenterTestOfferingRepository offeringRepository;

    public DiagnosticCenterServiceImpl(
            IDiagnosticCenterRepository centerRepository,
            IDiagnosticTestRepository testRepository,
            @Lazy IAppointmentRepository appointmentRepository,
            ICenterTestOfferingRepository offeringRepository) {
        this.centerRepository = centerRepository;
        this.testRepository = testRepository;
        this.appointmentRepository = appointmentRepository;
        this.offeringRepository = offeringRepository;
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
    public CenterTestOffering viewTestOffering(int diagnosticCenterId, String testName) {
        return offeringRepository.findByCenter_IdAndTest_TestName(diagnosticCenterId, testName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Test '" + testName + "' not offered at center id=" + diagnosticCenterId));
    }

    @Override
    @Transactional
    public CenterTestOffering addTest(int diagnosticCenterId, int testId, double price) {
        DiagnosticCenter center = getDiagnosticCenterById(diagnosticCenterId);
        DiagnosticTest test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticTest", "id", testId));
        CenterTestOffering offering = CenterTestOffering.builder()
                .id(new CenterTestOfferingKey(diagnosticCenterId, testId))
                .center(center)
                .test(test)
                .price(price)
                .build();
        return offeringRepository.save(offering);
    }

    @Override
    @Transactional
    public void removeTest(int diagnosticCenterId, int testId) {
        offeringRepository.deleteById(new CenterTestOfferingKey(diagnosticCenterId, testId));
    }
}
