package com.capstone.healthcare.shared.seeder;

import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.appointment.model.ApprovalStatus;
import com.capstone.healthcare.appointment.repository.IAppointmentRepository;
import com.capstone.healthcare.auth.model.Role;
import com.capstone.healthcare.auth.model.UserAccount;
import com.capstone.healthcare.auth.repository.IRoleRepository;
import com.capstone.healthcare.auth.repository.IUserRepository;
import com.capstone.healthcare.auth.service.IPasswordEncoderService;
import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnosticcenter.repository.IDiagnosticCenterRepository;
import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;
import com.capstone.healthcare.diagnostictest.model.TestCategory;
import com.capstone.healthcare.diagnostictest.repository.IDiagnosticTestRepository;
import com.capstone.healthcare.diagnostictest.repository.ITestCategoryRepository;
import com.capstone.healthcare.patient.model.Patient;
import com.capstone.healthcare.patient.repository.IPatientRepository;
import com.capstone.healthcare.shared.security.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Seeds the database with sample data on first startup (when roles table is
 * empty).
 * Skipped in "test" profile to avoid interfering with unit/integration tests.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final IRoleRepository roleRepository;
    private final IUserRepository userRepository;
    private final IPatientRepository patientRepository;
    private final ITestCategoryRepository categoryRepository;
    private final IDiagnosticTestRepository testRepository;
    private final IDiagnosticCenterRepository centerRepository;
    private final IAppointmentRepository appointmentRepository;
    private final IPasswordEncoderService passwordEncoderService;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("[DataSeeder] Database already seeded — skipping.");
            return;
        }

        log.info("[DataSeeder] Seeding database...");

        // ── 1. Roles ─────────────────────────────────────────────────────────
        Role customerRole = roleRepository.save(
                Role.builder().roleName(RoleConstants.CUSTOMER).build());
        Role adminRole = roleRepository.save(
                Role.builder().roleName(RoleConstants.ADMIN).build());

        // ── 2. Users ──────────────────────────────────────────────────────────
        UserAccount adminUser = userRepository.save(UserAccount.builder()
                .fullName("System Admin")
                .email("admin@healthcare.com")
                .phone("09000000000")
                .passwordHash(passwordEncoderService.encode("Admin@1234"))
                .status("ACTIVE")
                .roles(Set.of(adminRole))
                .build());

        UserAccount customerUser = userRepository.save(UserAccount.builder()
                .fullName("Juan Dela Cruz")
                .email("juan@example.com")
                .phone("09111111111")
                .passwordHash(passwordEncoderService.encode("Customer@1234"))
                .status("ACTIVE")
                .roles(Set.of(customerRole))
                .build());

        log.info("[DataSeeder] Created users: {} and {}", adminUser.getEmail(), customerUser.getEmail());

        // ── 3. Patient profile (linked to customer user by email as username) ──
        Patient patient = patientRepository.save(Patient.builder()
                .name("Juan Dela Cruz")
                .phoneNo("09111111111")
                .age(30)
                .gender("Male")
                .username(customerUser.getEmail())
                .build());

        log.info("[DataSeeder] Created patient: {}", patient.getName());

        // ── 4. Test Categories ────────────────────────────────────────────────
        TestCategory hematology = categoryRepository.save(TestCategory.builder()
                .categoryName("Hematology")
                .description("Blood and blood-forming tissue")
                .build());

        TestCategory biochemistry = categoryRepository.save(TestCategory.builder()
                .categoryName("Biochemistry")
                .description("Chemical processes in living organisms")
                .build());

        TestCategory microbiology = categoryRepository.save(TestCategory.builder()
                .categoryName("Microbiology")
                .description("Microorganism-related diagnostics")
                .build());

        log.info("[DataSeeder] Created test categories: Hematology, Biochemistry, Microbiology");

        // ── 5. Diagnostic Tests ───────────────────────────────────────────────
        DiagnosticTest cbc = testRepository.save(DiagnosticTest.builder()
                .testName("Complete Blood Count (CBC)")
                .testPrice(350.00)
                .normalValue("RBC: 4.5-5.5 M/uL")
                .units("M/uL")
                .status("ACTIVE")
                .category(hematology)
                .build());

        DiagnosticTest wbc = testRepository.save(DiagnosticTest.builder()
                .testName("White Blood Cell Differential")
                .testPrice(200.00)
                .normalValue("4.5-11.0 K/uL")
                .units("K/uL")
                .status("ACTIVE")
                .category(hematology)
                .build());

        DiagnosticTest bmp = testRepository.save(DiagnosticTest.builder()
                .testName("Basic Metabolic Panel (BMP)")
                .testPrice(500.00)
                .normalValue("Glucose: 70-100 mg/dL")
                .units("mg/dL")
                .status("ACTIVE")
                .category(biochemistry)
                .build());

        DiagnosticTest lipid = testRepository.save(DiagnosticTest.builder()
                .testName("Lipid Panel")
                .testPrice(450.00)
                .normalValue("Total Cholesterol <200 mg/dL")
                .units("mg/dL")
                .status("ACTIVE")
                .category(biochemistry)
                .build());

        DiagnosticTest urinalysis = testRepository.save(DiagnosticTest.builder()
                .testName("Urinalysis")
                .testPrice(150.00)
                .normalValue("pH: 4.5-8.5")
                .units("pH")
                .status("ACTIVE")
                .category(microbiology)
                .build());

        log.info("[DataSeeder] Created 5 diagnostic tests");

        // ── 6. Diagnostic Centers ─────────────────────────────────────────────
        DiagnosticCenter centerA = centerRepository.save(DiagnosticCenter.builder()
                .name("HealthFirst Diagnostics")
                .contactNo("028001234")
                .address("123 Ayala Ave, Makati City")
                .contactEmail("info@healthfirst.ph")
                .status("ACTIVE")
                .servicesOffered(List.of("Blood Tests", "Urinalysis", "ECG", "X-Ray"))
                .tests(Set.of(cbc, wbc, bmp, lipid, urinalysis))
                .build());

        DiagnosticCenter centerB = centerRepository.save(DiagnosticCenter.builder()
                .name("MedStar Laboratory")
                .contactNo("027009876")
                .address("456 Commonwealth Ave, Quezon City")
                .contactEmail("contact@medstar.ph")
                .status("ACTIVE")
                .servicesOffered(List.of("Blood Tests", "Microbiology", "Culture & Sensitivity"))
                .tests(Set.of(cbc, bmp, urinalysis))
                .build());

        DiagnosticCenter centerC = centerRepository.save(DiagnosticCenter.builder()
                .name("CityDiag Cebu")
                .contactNo("032001111")
                .address("789 Osmena Blvd, Cebu City")
                .contactEmail("hello@citydiag.ph")
                .status("ACTIVE")
                .servicesOffered(List.of("Blood Tests", "Lipid Profile", "Thyroid Tests"))
                .tests(Set.of(lipid, bmp, wbc))
                .build());

        log.info("[DataSeeder] Created 3 diagnostic centers");

        // ── 7. Sample Appointment ─────────────────────────────────────────────
        Appointment appointment = appointmentRepository.save(Appointment.builder()
                .patient(patient)
                .diagnosticCenter(centerA)
                .diagnosticTests(Set.of(cbc, lipid))
                .appointmentDate(LocalDate.now().plusDays(7))
                .approvalStatus(ApprovalStatus.PENDING)
                .remarks("First-time checkup")
                .build());

        log.info("[DataSeeder] Created sample appointment id={} for patient '{}' at '{}'",
                appointment.getId(), patient.getName(), centerA.getName());

        log.info("[DataSeeder] Seeding complete.");
    }
}
