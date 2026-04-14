package com.capstone.healthcare.shared.seeder;

import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.appointment.model.ApprovalStatus;
import com.capstone.healthcare.appointment.repository.IAppointmentRepository;
import com.capstone.healthcare.auth.model.Role;
import com.capstone.healthcare.auth.model.UserAccount;
import com.capstone.healthcare.auth.repository.IRoleRepository;
import com.capstone.healthcare.auth.repository.IUserRepository;
import com.capstone.healthcare.auth.service.IPasswordEncoderService;
import com.capstone.healthcare.diagnosticcenter.model.CenterTestOffering;
import com.capstone.healthcare.diagnosticcenter.model.CenterTestOfferingKey;
import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnosticcenter.repository.ICenterTestOfferingRepository;
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
import java.util.*;

/**
 * Seeds the database with realistic sample data on first startup.
 * Skipped in "test" profile to avoid interfering with unit/integration tests.
 *
 * Produces:
 * - 5 test categories, 12 diagnostic tests
 * - 7 diagnostic centers
 * - 1-2 center admins per center, password: admin@1234
 * - 1-5 center staff per center, password: staff@1234
 * - 1 primary admin account, password: Admin@1234
 * - 100 patient accounts + profiles, password: Patient@1234
 * - 500 appointments (50-75 with special requests)
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
        private final ICenterTestOfferingRepository offeringRepository;
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
                Random rng = new Random(42);

                // ── 1. Roles ─────────────────────────────────────────────────────────
                Role customerRole = roleRepository.save(Role.builder().roleName(RoleConstants.CUSTOMER).build());
                Role adminRole = roleRepository.save(Role.builder().roleName(RoleConstants.ADMIN).build());
                Role centerAdminRole = roleRepository.save(Role.builder().roleName(RoleConstants.CENTER_ADMIN).build());
                Role centerStaffRole = roleRepository.save(Role.builder().roleName(RoleConstants.CENTER_STAFF).build());

                // ── 2. Primary admin ──────────────────────────────────────────────────
                userRepository.save(UserAccount.builder()
                                .fullName("System Admin")
                                .email("admin@healthcare.com")
                                .phone("09000000000")
                                .passwordHash(passwordEncoderService.encode("Admin@1234"))
                                .status("ACTIVE")
                                .roles(Set.of(adminRole))
                                .build());
                log.info("[DataSeeder] Primary admin: admin@healthcare.com / Admin@1234");

                // ── 3. Test categories (5) ────────────────────────────────────────────
                TestCategory hematology = categoryRepository
                                .save(cat("Hematology", "Blood and blood-forming tissue tests"));
                TestCategory biochemistry = categoryRepository
                                .save(cat("Biochemistry", "Chemical processes and metabolic panels"));
                TestCategory microbiology = categoryRepository
                                .save(cat("Microbiology", "Bacterial, viral and fungal diagnostics"));
                TestCategory radiology = categoryRepository
                                .save(cat("Radiology", "Imaging and radiological procedures"));
                TestCategory cardiology = categoryRepository
                                .save(cat("Cardiology", "Heart and cardiovascular diagnostics"));
                log.info("[DataSeeder] Created 5 test categories");

                // ── 4. Diagnostic tests (12) ─────────────────────────────────────────
                DiagnosticTest cbc = t("Complete Blood Count (CBC)", 350, hematology);
                DiagnosticTest hba1c = t("Hemoglobin A1c", 420, hematology);
                DiagnosticTest lipid = t("Lipid Panel", 450, biochemistry);
                DiagnosticTest thyroid = t("Thyroid Panel (TSH/T3/T4)", 650, biochemistry);
                DiagnosticTest liverFunc = t("Liver Function Tests (LFT)", 600, biochemistry);
                DiagnosticTest kidneyFunc = t("Kidney Function Tests (KFT)", 550, biochemistry);
                DiagnosticTest urinalysis = t("Urinalysis (Complete)", 150, microbiology);
                DiagnosticTest urineCulture = t("Urine Culture & Sensitivity", 480, microbiology);
                DiagnosticTest chestXray = t("Chest X-Ray (PA View)", 500, radiology);
                DiagnosticTest ultrasound = t("Abdominal Ultrasound", 1200, radiology);
                DiagnosticTest ecg = t("12-Lead Electrocardiogram (ECG)", 450, cardiology);
                DiagnosticTest echo = t("2D Echocardiography", 3500, cardiology);

                List<DiagnosticTest> allTests = testRepository.saveAll(List.of(
                                cbc, hba1c, lipid, thyroid, liverFunc, kidneyFunc,
                                urinalysis, urineCulture, chestXray, ultrasound, ecg, echo));
                log.info("[DataSeeder] Created {} diagnostic tests", allTests.size());

                // ── 5. Diagnostic centers (7) + admins + staff ────────────────────────
                record CenterDef(String name, String phone, String address, String email,
                                String slug, List<String> services, List<DiagnosticTest> tests,
                                int adminCount, int staffCount) {
                }

                List<CenterDef> defs = List.of(
                                new CenterDef("HealthFirst Diagnostics", "028001234",
                                                "123 Ayala Ave, Makati City", "info@healthfirst.ph",
                                                "healthfirst",
                                                List.of("Blood Tests", "ECG", "X-Ray", "Ultrasound"),
                                                List.of(cbc, hba1c, lipid, ecg, chestXray, ultrasound, urinalysis),
                                                2, 5),
                                new CenterDef("MedStar Laboratory", "027009876",
                                                "456 Commonwealth Ave, Quezon City", "contact@medstar.ph",
                                                "medstar",
                                                List.of("Blood Tests", "Microbiology", "Culture & Sensitivity"),
                                                List.of(cbc, lipid, urinalysis, urineCulture, liverFunc, kidneyFunc),
                                                1, 3),
                                new CenterDef("CityDiag Cebu", "032001111",
                                                "789 Osmena Blvd, Cebu City", "hello@citydiag.ph",
                                                "citydiag",
                                                List.of("Blood Tests", "Thyroid Tests", "Imaging"),
                                                List.of(cbc, lipid, thyroid, ultrasound, chestXray, liverFunc,
                                                                kidneyFunc, ecg),
                                                2, 4),
                                new CenterDef("Sunrise Diagnostics Center", "022223333",
                                                "10 Taft Ave, Ermita, Manila", "info@sunrisediag.ph",
                                                "sunrise",
                                                List.of("Hematology", "Biochemistry", "Cardiology"),
                                                List.of(cbc, hba1c, lipid, liverFunc, kidneyFunc, ecg, echo),
                                                1, 2),
                                new CenterDef("Davao Health Diagnostics", "082111222",
                                                "55 JP Laurel Ave, Davao City", "davaohealth@diag.ph",
                                                "davaohealth",
                                                List.of("Full Blood Panel", "Microbiology", "X-Ray"),
                                                List.of(cbc, urinalysis, urineCulture, chestXray, liverFunc,
                                                                kidneyFunc),
                                                2, 5),
                                new CenterDef("Laguna MedLab", "049778899",
                                                "3 Gomez St, Santa Rosa, Laguna", "info@lagunamedlab.ph",
                                                "lagunamedlab",
                                                List.of("Blood Tests", "Urinalysis", "Thyroid"),
                                                List.of(cbc, hba1c, thyroid, lipid, urinalysis, kidneyFunc, liverFunc),
                                                1, 4),
                                new CenterDef("Pampanga Diagnostic Hub", "045334455",
                                                "7 MacArthur Hwy, Angeles City", "hub@pampangadiag.ph",
                                                "pampangahub",
                                                List.of("Imaging", "Cardiology", "Blood Tests"),
                                                List.of(cbc, lipid, ultrasound, chestXray, ecg, echo, hba1c),
                                                2, 1));

                List<DiagnosticCenter> centers = new ArrayList<>();
                Map<DiagnosticCenter, List<DiagnosticTest>> centerTestsMap = new HashMap<>();

                String[] staffNames = { "Alex", "Blake", "Casey", "Dana", "Eden" };

                for (int ci = 0; ci < defs.size(); ci++) {
                        CenterDef d = defs.get(ci);

                        DiagnosticCenter center = centerRepository.save(DiagnosticCenter.builder()
                                        .name(d.name())
                                        .contactNo(d.phone())
                                        .address(d.address())
                                        .contactEmail(d.email())
                                        .status("ACTIVE")
                                        .servicesOffered(d.services())
                                        .build());
                        centers.add(center);
                        centerTestsMap.put(center, d.tests());

                        // Per-center test offerings with ±20 % price variation
                        for (DiagnosticTest test : d.tests()) {
                                double variation = 0.8 + rng.nextDouble() * 0.4;
                                double price = Math.round(test.getTestPrice() * variation / 10.0) * 10.0;
                                offeringRepository.save(CenterTestOffering.builder()
                                                .id(new CenterTestOfferingKey(center.getId(), test.getId()))
                                                .center(center)
                                                .test(test)
                                                .price(price)
                                                .build());
                        }

                        // Center admins (1-2)
                        for (int a = 0; a < d.adminCount(); a++) {
                                String adminEmail = (a == 0 ? "admin." : "admin" + (a + 1) + ".") + d.slug()
                                                + "@healthcare.ph";
                                userRepository.save(UserAccount.builder()
                                                .fullName(d.name() + " Admin" + (a == 0 ? "" : " " + (a + 1)))
                                                .email(adminEmail)
                                                .phone("09" + String.format("%09d", 100000000L + ci * 10 + a))
                                                .passwordHash(passwordEncoderService.encode("admin@1234"))
                                                .status("ACTIVE")
                                                .centerId(center.getId())
                                                .roles(Set.of(centerAdminRole))
                                                .build());
                                log.info("[DataSeeder] {} | Admin: {}", d.name(), adminEmail);
                        }

                        // Center staff (1-5)
                        for (int st = 0; st < d.staffCount(); st++) {
                                String staffEmail = "staff" + (st + 1) + "." + d.slug() + "@healthcare.ph";
                                userRepository.save(UserAccount.builder()
                                                .fullName(staffNames[st % staffNames.length] + " (" + d.slug() + ")")
                                                .email(staffEmail)
                                                .phone("09" + String.format("%09d", 300000000L + ci * 10 + st))
                                                .passwordHash(passwordEncoderService.encode("staff@1234"))
                                                .status("ACTIVE")
                                                .centerId(center.getId())
                                                .roles(Set.of(centerStaffRole))
                                                .build());
                                log.info("[DataSeeder] {} | Staff: {}", d.name(), staffEmail);
                        }
                }
                log.info("[DataSeeder] Created {} centers with admins & staff", centers.size());

                // ── 6. Patients (100) ─────────────────────────────────────────────────
                String[] firstNames = {
                                "Juan", "Maria", "Jose", "Ana", "Pedro", "Rosa", "Carlos", "Luisa",
                                "Ramon", "Elena", "Antonio", "Clara", "Miguel", "Lucia", "Fernando",
                                "Isabel", "Roberto", "Carmen", "Luis", "Teresa", "Manuel", "Patricia",
                                "Eduardo", "Monica", "Rafael", "Angela", "Andres", "Alicia", "Pablo",
                                "Gloria", "Ricardo", "Marta", "Jorge", "Nora", "Alberto", "Silvia",
                                "Francisco", "Beatriz", "Enrique", "Pilar", "Raul", "Cristina",
                                "David", "Sandra", "Sergio", "Vanessa", "Diego", "Laura", "Oscar",
                                "Natalia"
                };
                String[] lastNames = {
                                "Santos", "Reyes", "Cruz", "Garcia", "Dela Cruz", "Bautista", "Ramos",
                                "Aquino", "Mendoza", "Villanueva", "Torres", "Flores", "Lopez",
                                "Ramirez", "Hernandez", "Gonzalez", "Perez", "Morales", "Jimenez",
                                "Ortiz", "Diaz", "Castillo", "Navarro", "Ruiz", "Vega", "Guerrero",
                                "Medina", "Vargas", "Dominguez", "Soto", "Estrada", "Padilla",
                                "Salazar", "Aguilar", "Fuentes", "Rios", "Campos", "Espinoza",
                                "Cano", "Contreras", "Miranda", "Lozano", "Molina", "Silva",
                                "Ferrer", "Castro", "Romero", "Guzman", "Munoz", "Moreno"
                };
                String[] genders = { "Male", "Female" };

                List<Patient> patients = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                        String first = firstNames[i % firstNames.length];
                        String last = lastNames[i % lastNames.length];
                        String fullName = first + " " + last;
                        String email = first.toLowerCase() + "." + last.toLowerCase().replace(" ", "") + i
                                        + "@example.com";
                        String phone = "09" + String.format("%09d", 200000000L + i);

                        userRepository.save(UserAccount.builder()
                                        .fullName(fullName)
                                        .email(email)
                                        .phone(phone)
                                        .passwordHash(passwordEncoderService.encode("Patient@1234"))
                                        .status("ACTIVE")
                                        .roles(Set.of(customerRole))
                                        .build());

                        patients.add(patientRepository.save(Patient.builder()
                                        .name(fullName)
                                        .phoneNo(phone)
                                        .age(18 + (i % 60))
                                        .gender(genders[i % 2])
                                        .username(email)
                                        .build()));
                }
                log.info("[DataSeeder] Created {} patients", patients.size());

                // ── 7. Appointments (500, 50-75 with special requests) ────────────────
                ApprovalStatus[] statuses = {
                                ApprovalStatus.APPROVED, ApprovalStatus.APPROVED, ApprovalStatus.APPROVED,
                                ApprovalStatus.PENDING, ApprovalStatus.PENDING,
                                ApprovalStatus.REJECTED,
                                ApprovalStatus.CANCELLED
                };

                String[] adminRemarks = {
                                null, null, null, null, null, null, null, null, null, null,
                                "Please arrive 30 minutes early",
                                "Fasting required — no food 8 hours before",
                                "Bring previous lab results",
                                "Insurance pre-authorization confirmed",
                                "Follow-up in 2 weeks after results"
                };

                String[] specialRequestPool = {
                                "Wheelchair access required",
                                "Please provide a stretcher for transfer",
                                "Patient is visually impaired — needs guide assistance",
                                "Hearing impaired — written instructions preferred",
                                "Crutches available for mobility assistance",
                                "Patient has severe anxiety — please allow companion in room",
                                "Pregnant — please avoid X-ray exposure",
                                "Patient requires interpreter (sign language)",
                                "Elderly patient — please allow extra time",
                                "Patient has needle phobia — gentle approach appreciated"
                };

                // Decide which appointment indices get a special request (exactly 60)
                Set<Integer> specialRequestIndices = new HashSet<>();
                while (specialRequestIndices.size() < 60) {
                        specialRequestIndices.add(rng.nextInt(500));
                }

                int appointmentCount = 0;
                for (int i = 0; i < 500; i++) {
                        Patient p = patients.get(rng.nextInt(patients.size()));
                        DiagnosticCenter c = centers.get(i % centers.size());
                        ApprovalStatus status = statuses[i % statuses.length];

                        List<DiagnosticTest> cTests = centerTestsMap.get(c);
                        int numTests = 1 + rng.nextInt(Math.min(3, cTests.size()));
                        List<DiagnosticTest> shuffled = new ArrayList<>(cTests);
                        Collections.shuffle(shuffled, rng);
                        Set<DiagnosticTest> chosenTests = new HashSet<>(shuffled.subList(0, numTests));

                        LocalDate date = switch (status) {
                                case PENDING -> LocalDate.now().plusDays(1 + rng.nextInt(45));
                                case APPROVED -> LocalDate.now().minusDays(1 + rng.nextInt(150));
                                case REJECTED -> LocalDate.now().minusDays(5 + rng.nextInt(90));
                                case CANCELLED -> LocalDate.now().minusDays(2 + rng.nextInt(60));
                        };

                        String remark = adminRemarks[rng.nextInt(adminRemarks.length)];
                        String specialReq = specialRequestIndices.contains(i)
                                        ? specialRequestPool[rng.nextInt(specialRequestPool.length)]
                                        : null;

                        appointmentRepository.save(Appointment.builder()
                                        .patient(p)
                                        .diagnosticCenter(c)
                                        .diagnosticTests(chosenTests)
                                        .appointmentDate(date)
                                        .approvalStatus(status)
                                        .remarks(remark)
                                        .specialRequests(specialReq)
                                        .build());
                        appointmentCount++;
                }

                log.info("[DataSeeder] Created {} appointments ({} with special requests)",
                                appointmentCount, specialRequestIndices.size());
                log.info("[DataSeeder] Seeding complete.");
        }

        // ── Helpers ───────────────────────────────────────────────────────────────

        private TestCategory cat(String name, String desc) {
                return TestCategory.builder().categoryName(name).description(desc).build();
        }

        private DiagnosticTest t(String name, double price, TestCategory cat) {
                return DiagnosticTest.builder()
                                .testName(name)
                                .testPrice(price)
                                .status("ACTIVE")
                                .category(cat)
                                .build();
        }
}
