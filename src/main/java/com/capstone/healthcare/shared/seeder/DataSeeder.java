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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Seeds the database with realistic sample data on first startup.
 * Skipped in "test" profile to avoid interfering with unit/integration tests.
 *
 * Produces:
 * - 7 test categories, 45 diagnostic tests
 * - 15 diagnostic centers spread across the Philippines
 * - 15 center-admin accounts (one per center), password: admin@1234
 * - 1 primary admin account, password: Admin@1234
 * - 150 patient accounts + patient profiles, password: Patient@1234
 * - ~400 appointments distributed across all statuses and centers
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
                Random rng = new Random(42); // deterministic

                // ── 1. Roles ─────────────────────────────────────────────────────────
                Role customerRole = roleRepository.save(Role.builder().roleName(RoleConstants.CUSTOMER).build());
                Role adminRole = roleRepository.save(Role.builder().roleName(RoleConstants.ADMIN).build());
                Role centerAdminRole = roleRepository.save(Role.builder().roleName(RoleConstants.CENTER_ADMIN).build());

                // ── 2. Primary admin ──────────────────────────────────────────────────
                userRepository.save(UserAccount.builder()
                                .fullName("System Admin")
                                .email("admin@healthcare.com")
                                .phone("09000000000")
                                .passwordHash(passwordEncoderService.encode("Admin@1234"))
                                .status("ACTIVE")
                                .roles(Set.of(adminRole))
                                .build());

                log.info("[DataSeeder] Primary admin created — admin@healthcare.com / Admin@1234");

                // ── 3. Test categories ────────────────────────────────────────────────
                TestCategory hematology = cat("Hematology", "Blood and blood-forming tissue tests");
                TestCategory biochemistry = cat("Biochemistry", "Chemical processes and metabolic panels");
                TestCategory microbiology = cat("Microbiology", "Bacterial, viral and fungal diagnostics");
                TestCategory immunology = cat("Immunology", "Immune system and antibody tests");
                TestCategory radiology = cat("Radiology", "Imaging and radiological procedures");
                TestCategory pathology = cat("Pathology", "Tissue and cell examination");
                TestCategory cardiology = cat("Cardiology", "Heart and cardiovascular diagnostics");

                hematology = categoryRepository.save(hematology);
                biochemistry = categoryRepository.save(biochemistry);
                microbiology = categoryRepository.save(microbiology);
                immunology = categoryRepository.save(immunology);
                radiology = categoryRepository.save(radiology);
                pathology = categoryRepository.save(pathology);
                cardiology = categoryRepository.save(cardiology);

                log.info("[DataSeeder] Created 7 test categories");

                // ── 4. Diagnostic tests (45 total) ────────────────────────────────────

                // Hematology (8)
                DiagnosticTest cbc = t("Complete Blood Count (CBC)", 350, "RBC: 4.5-5.5 M/uL", "M/uL", hematology);
                DiagnosticTest wbcDiff = t("White Blood Cell Differential", 200, "4.5-11.0 K/uL", "K/uL", hematology);
                DiagnosticTest esr = t("Erythrocyte Sedimentation Rate (ESR)", 180, "0-20 mm/hr", "mm/hr", hematology);
                DiagnosticTest hba1c = t("Hemoglobin A1c", 420, "4.0-5.6%", "%", hematology);
                DiagnosticTest reticulocyte = t("Reticulocyte Count", 250, "0.5-2.5%", "%", hematology);
                DiagnosticTest coagulation = t("Coagulation Panel (PT/PTT/INR)", 550, "PT: 10-13 sec", "sec",
                                hematology);
                DiagnosticTest platelet = t("Platelet Count", 180, "150-400 K/uL", "K/uL", hematology);
                DiagnosticTest bloodType = t("Blood Typing & Cross-matching", 300, "ABO/Rh", "type", hematology);

                // Biochemistry (9)
                DiagnosticTest bmp = t("Basic Metabolic Panel (BMP)", 500, "Glucose: 70-100 mg/dL", "mg/dL",
                                biochemistry);
                DiagnosticTest cmp = t("Comprehensive Metabolic Panel (CMP)", 700, "ALT: 7-56 U/L", "U/L",
                                biochemistry);
                DiagnosticTest lipid = t("Lipid Panel", 450, "Total Cholesterol <200", "mg/dL", biochemistry);
                DiagnosticTest thyroid = t("Thyroid Panel (TSH/T3/T4)", 650, "TSH: 0.4-4.0 mIU/L", "mIU/L",
                                biochemistry);
                DiagnosticTest liverFunc = t("Liver Function Tests (LFT)", 600, "ALT: 7-56 U/L", "U/L", biochemistry);
                DiagnosticTest kidneyFunc = t("Kidney Function Tests (KFT/BUN/Creat)", 550, "Creatinine: 0.7-1.3 mg/dL",
                                "mg/dL", biochemistry);
                DiagnosticTest uricAcid = t("Uric Acid", 220, "3.5-7.2 mg/dL", "mg/dL", biochemistry);
                DiagnosticTest glucose = t("Fasting Blood Glucose (FBG)", 150, "70-100 mg/dL", "mg/dL", biochemistry);
                DiagnosticTest hsCRP = t("High-Sensitivity C-Reactive Protein", 380, "<1.0 mg/L low risk", "mg/L",
                                biochemistry);

                // Microbiology (6)
                DiagnosticTest urinalysis = t("Urinalysis (Complete)", 150, "pH: 4.5-8.5", "pH", microbiology);
                DiagnosticTest urineCulture = t("Urine Culture & Sensitivity", 480, "No growth", "CFU/mL",
                                microbiology);
                DiagnosticTest stoolExam = t("Fecalysis / Stool Examination", 120, "No ova/parasites", "—",
                                microbiology);
                DiagnosticTest throatSwab = t("Throat Swab Culture", 350, "No pathogen isolated", "—", microbiology);
                DiagnosticTest woundCulture = t("Wound Culture & Sensitivity", 520, "No growth", "CFU/mL",
                                microbiology);
                DiagnosticTest tb = t("Tuberculosis (TB) Detection (GeneXpert)", 800, "Not detected", "—",
                                microbiology);

                // Immunology (6)
                DiagnosticTest hivTest = t("HIV 1&2 Antibody Test", 500, "Non-reactive", "—", immunology);
                DiagnosticTest hepatitisB = t("Hepatitis B Surface Antigen (HBsAg)", 350, "Non-reactive", "—",
                                immunology);
                DiagnosticTest hepatitisC = t("Hepatitis C Antibody (Anti-HCV)", 400, "Non-reactive", "—", immunology);
                DiagnosticTest dengue = t("Dengue NS1 Antigen & IgG/IgM", 650, "Non-reactive", "—", immunology);
                DiagnosticTest covid = t("COVID-19 RT-PCR Test", 900, "Not detected", "—", immunology);
                DiagnosticTest aso = t("ASO Titer (Anti-Streptolysin O)", 420, "<200 IU/mL", "IU/mL", immunology);

                // Radiology (5)
                DiagnosticTest chestXray = t("Chest X-Ray (PA View)", 500, "Clear lung fields", "—", radiology);
                DiagnosticTest abdominalXray = t("Abdominal X-Ray (KUB)", 650, "No abnormality detected", "—",
                                radiology);
                DiagnosticTest boneXray = t("Bone X-Ray", 600, "No fracture", "—", radiology);
                DiagnosticTest ultrasound = t("Abdominal Ultrasound", 1200, "Normal echogenicity", "—", radiology);
                DiagnosticTest mri = t("MRI Brain (Plain)", 5500, "No intracranial lesion", "—", radiology);

                // Pathology (5)
                DiagnosticTest papSmear = t("Pap Smear / Cervical Cytology", 600, "NILM — No lesion", "—", pathology);
                DiagnosticTest biopsy = t("Tissue Biopsy (Routine H&E)", 1800, "No malignancy", "—", pathology);
                DiagnosticTest fnab = t("Fine Needle Aspiration Biopsy (FNAB)", 1500, "No malignancy", "—", pathology);
                DiagnosticTest urineHisto = t("Urine Cytology", 850, "No malignant cells", "—", pathology);
                DiagnosticTest semenAnalysis = t("Semen Analysis", 750, "Vol ≥1.5 mL, Motility ≥40%", "—", pathology);

                // Cardiology (6)
                DiagnosticTest ecg = t("12-Lead Electrocardiogram (ECG)", 450, "Normal sinus rhythm", "—", cardiology);
                DiagnosticTest echo = t("2D Echocardiography", 3500, "Normal cardiac function", "—", cardiology);
                DiagnosticTest holter = t("24-Hour Holter Monitor", 2800, "No significant arrhythmia", "—", cardiology);
                DiagnosticTest troponin = t("Troponin I / T", 700, "<0.04 ng/mL", "ng/mL", cardiology);
                DiagnosticTest bnp = t("Brain Natriuretic Peptide (BNP)", 950, "<100 pg/mL", "pg/mL", cardiology);
                DiagnosticTest stressTest = t("Treadmill Stress Test (TMT)", 2500, "Negative for ischemia", "—",
                                cardiology);

                List<DiagnosticTest> allTests = testRepository.saveAll(List.of(
                                cbc, wbcDiff, esr, hba1c, reticulocyte, coagulation, platelet, bloodType,
                                bmp, cmp, lipid, thyroid, liverFunc, kidneyFunc, uricAcid, glucose, hsCRP,
                                urinalysis, urineCulture, stoolExam, throatSwab, woundCulture, tb,
                                hivTest, hepatitisB, hepatitisC, dengue, covid, aso,
                                chestXray, abdominalXray, boneXray, ultrasound, mri,
                                papSmear, biopsy, fnab, urineHisto, semenAnalysis,
                                ecg, echo, holter, troponin, bnp, stressTest));

                log.info("[DataSeeder] Created {} diagnostic tests", allTests.size());

                // ── 5. Diagnostic Centers (15) + Center Admins ────────────────────────

                record CenterSpec(
                                String name, String phone, String address, String email, String city,
                                String slug, List<String> services, List<DiagnosticTest> tests) {
                }

                List<CenterSpec> specs = List.of(
                                new CenterSpec(
                                                "HealthFirst Diagnostics", "028001234",
                                                "123 Ayala Ave, Makati City", "info@healthfirst.ph", "Makati",
                                                "healthfirst",
                                                List.of("Blood Tests", "Urinalysis", "ECG", "X-Ray", "Ultrasound"),
                                                List.of(cbc, wbcDiff, bmp, lipid, urinalysis, ecg, chestXray,
                                                                ultrasound, glucose, hba1c)),
                                new CenterSpec(
                                                "MedStar Laboratory", "027009876",
                                                "456 Commonwealth Ave, Quezon City", "contact@medstar.ph", "QC",
                                                "medstar",
                                                List.of("Blood Tests", "Microbiology", "Culture & Sensitivity"),
                                                List.of(cbc, bmp, urinalysis, urineCulture, stoolExam, throatSwab, esr,
                                                                hsCRP, bloodType, platelet)),
                                new CenterSpec(
                                                "CityDiag Cebu", "032001111",
                                                "789 Osmena Blvd, Cebu City", "hello@citydiag.ph", "Cebu",
                                                "citydiag",
                                                List.of("Blood Tests", "Lipid Profile", "Thyroid Tests", "Imaging"),
                                                List.of(lipid, thyroid, bmp, wbcDiff, ultrasound, chestXray, ecg,
                                                                liverFunc, kidneyFunc, uricAcid)),
                                new CenterSpec(
                                                "Sunrise Diagnostics Center", "022223333",
                                                "10 Taft Ave, Ermita, Manila", "info@sunrisediag.ph", "Manila",
                                                "sunrise",
                                                List.of("Hematology", "Biochemistry", "Immunology"),
                                                List.of(cbc, hba1c, coagulation, hivTest, hepatitisB, hepatitisC, bmp,
                                                                liverFunc, dengue, covid)),
                                new CenterSpec(
                                                "PrimeCare Medical Laboratory", "034567890",
                                                "22 Rizal St, Bacolod City", "primecare@ph.net", "Bacolod",
                                                "primecare",
                                                List.of("Blood Tests", "Urinalysis", "Stool Exam", "ECG"),
                                                List.of(cbc, urinalysis, stoolExam, ecg, glucose, cholesterol(lipid),
                                                                esr, platelet, wbcDiff, bmp)),
                                new CenterSpec(
                                                "Davao Health Diagnostics", "082111222",
                                                "55 JP Laurel Ave, Davao City", "davaohealth@diag.ph", "Davao",
                                                "davaohealth",
                                                List.of("Full Blood Panel", "TB Detection", "Microbiology", "X-Ray"),
                                                List.of(cbc, tb, woundCulture, urineCulture, chestXray, abdominalXray,
                                                                boneXray, bmp, kidneyFunc, liverFunc)),
                                new CenterSpec(
                                                "Metro Diagnostics Iloilo", "033445566",
                                                "18 Iznart St, Iloilo City", "metro@iloilodiag.ph", "Iloilo",
                                                "metroiloilo",
                                                List.of("Immunology", "Cardiology", "Hematology"),
                                                List.of(hivTest, hepatitisB, aso, ecg, echo, troponin, cbc, esr,
                                                                reticulocyte, platelet)),
                                new CenterSpec(
                                                "Laguna MedLab", "049778899",
                                                "3 Gomez St, Santa Rosa, Laguna", "info@lagunamedlab.ph", "Laguna",
                                                "lagunamedlab",
                                                List.of("Blood Tests", "Urinalysis", "Thyroid", "Cholesterol"),
                                                List.of(cbc, thyroid, lipid, urinalysis, glucose, hba1c, kidneyFunc,
                                                                uricAcid, hsCRP, bmp)),
                                new CenterSpec(
                                                "Cagayan Valley Lab Center", "078112233",
                                                "99 Mabini St, Tuguegarao City", "cvlc@cagayan.ph", "Cagayan",
                                                "cvlc",
                                                List.of("Hematology", "Biochemistry", "Urinalysis", "Stool Exam"),
                                                List.of(cbc, wbcDiff, bmp, urinalysis, stoolExam, bloodType, platelet,
                                                                glucose, liverFunc, esr)),
                                new CenterSpec(
                                                "Pampanga Diagnostic Hub", "045334455",
                                                "7 MacArthur Hwy, Angeles City", "hub@pampangadiag.ph", "Pampanga",
                                                "pampangahub",
                                                List.of("Full Blood Panel", "Imaging", "Cardiology"),
                                                List.of(cbc, lipid, bmp, ultrasound, chestXray, ecg, holter, stressTest,
                                                                troponin, bnp)),
                                new CenterSpec(
                                                "Batangas Medical Laboratory", "043556677",
                                                "12 P. Burgos St, Batangas City", "bml@batangas.ph", "Batangas",
                                                "bml",
                                                List.of("Immunology", "Microbiology", "Hematology"),
                                                List.of(hivTest, hepatitisC, hepatitisB, covid, dengue, urineCulture,
                                                                cbc, coagulation, esr, aso)),
                                new CenterSpec(
                                                "Zamboanga Diagnostic Center", "062778899",
                                                "40 La Purisima St, Zamboanga City", "zdc@zamboanga.ph", "Zamboanga",
                                                "zdc",
                                                List.of("Blood Tests", "Urinalysis", "X-Ray", "TB Detection"),
                                                List.of(cbc, urinalysis, chestXray, tb, bmp, glucose, thyroid,
                                                                liverFunc, stoolExam, kidneyFunc)),
                                new CenterSpec(
                                                "Baguio Highland MedLab", "074223344",
                                                "5 Session Rd, Baguio City", "highland@baguiomedlab.ph", "Baguio",
                                                "baguiomed",
                                                List.of("Blood Tests", "Pathology", "Thyroid Tests"),
                                                List.of(cbc, thyroid, papSmear, biopsy, fnab, urineHisto, liverFunc,
                                                                kidneyFunc, bmp, lipid)),
                                new CenterSpec(
                                                "Tacloban Precision Diagnostics", "053445566",
                                                "8 Real St, Tacloban City", "tpd@tacloban.ph", "Tacloban",
                                                "tpd",
                                                List.of("Blood Tests", "Immunology", "Biochemistry"),
                                                List.of(cbc, hivTest, hepatitisB, bmp, liverFunc, kidneyFunc, glucose,
                                                                hba1c, thyroid, uricAcid)),
                                new CenterSpec(
                                                "General Santos MedCheck", "083667788",
                                                "25 Pioneer Ave, General Santos City", "medcheck@gensan.ph", "GenSan",
                                                "gensan",
                                                List.of("Full Blood Panel", "Cardiology", "Imaging", "Pathology"),
                                                List.of(cbc, ecg, echo, stressTest, chestXray, ultrasound, papSmear,
                                                                biopsy, lipid, troponin)));

                List<DiagnosticCenter> centers = new ArrayList<>();
                Map<DiagnosticCenter, List<DiagnosticTest>> centerTestsMap = new HashMap<>();
                for (int i = 0; i < specs.size(); i++) {
                        CenterSpec s = specs.get(i);

                        DiagnosticCenter center = centerRepository.save(DiagnosticCenter.builder()
                                        .name(s.name())
                                        .contactNo(s.phone())
                                        .address(s.address())
                                        .contactEmail(s.email())
                                        .status("ACTIVE")
                                        .servicesOffered(s.services())
                                        .build());
                        centers.add(center);
                        centerTestsMap.put(center, s.tests());

                        // Per-center test offerings with ±20% price variation
                        for (DiagnosticTest test : s.tests()) {
                                double variation = 0.8 + rng.nextDouble() * 0.4; // 0.8–1.2
                                double price = Math.round(test.getTestPrice() * variation / 10.0) * 10.0;
                                offeringRepository.save(CenterTestOffering.builder()
                                                .id(new CenterTestOfferingKey(center.getId(), test.getId()))
                                                .center(center)
                                                .test(test)
                                                .price(price)
                                                .build());
                        }

                        // Center admin account
                        String adminEmail = "admin." + s.slug() + "@healthcare.ph";
                        String adminName = s.city() + " Admin";
                        userRepository.save(UserAccount.builder()
                                        .fullName(adminName)
                                        .email(adminEmail)
                                        .phone("09" + String.format("%09d", 100000000L + i))
                                        .passwordHash(passwordEncoderService.encode("admin@1234"))
                                        .status("ACTIVE")
                                        .centerId(center.getId())
                                        .roles(Set.of(centerAdminRole))
                                        .build());

                        log.info("[DataSeeder] Center: {} | Admin: {}", center.getName(), adminEmail);
                }

                log.info("[DataSeeder] Created {} centers with center admins", centers.size());

                // ── 6. Patients (150) ─────────────────────────────────────────────────

                String[] firstNames = {
                                "Juan", "Maria", "Jose", "Ana", "Pedro", "Rosa", "Carlos", "Luisa", "Ramon", "Elena",
                                "Antonio", "Clara", "Miguel", "Lucia", "Fernando", "Isabel", "Roberto", "Carmen",
                                "Luis", "Teresa",
                                "Manuel", "Patricia", "Eduardo", "Monica", "Rafael", "Angela", "Andres", "Alicia",
                                "Pablo", "Gloria",
                                "Ricardo", "Marta", "Jorge", "Nora", "Alberto", "Silvia", "Francisco", "Beatriz",
                                "Enrique", "Pilar",
                                "Raul", "Cristina", "David", "Sandra", "Sergio", "Vanessa", "Diego", "Laura", "Oscar",
                                "Natalia",
                                "Marco", "Lorena", "Javier", "Adriana", "Daniel", "Camila", "Felipe", "Valentina",
                                "Alejandro", "Sofia",
                                "Rene", "Michelle", "Arjun", "Priya", "Wei", "Lin", "James", "Grace", "Kevin", "Hannah",
                                "Nathan", "Abby", "Tyler", "Emma", "Ryan", "Olivia", "Joshua", "Ava", "Ethan", "Mia",
                                "Aaron", "Sophia", "Dylan", "Isabella", "Brandon", "Emily", "Justin", "Charlotte",
                                "Kyle", "Amelia",
                                "Christian", "Ella", "Matthew", "Chloe", "Andrew", "Lily", "Nicholas", "Zoe", "Patrick",
                                "Nadia"
                };
                String[] lastNames = {
                                "Santos", "Reyes", "Cruz", "Garcia", "Dela Cruz", "Bautista", "Ramos", "Aquino",
                                "Mendoza", "Villanueva",
                                "Torres", "Flores", "Lopez", "Ramirez", "Hernandez", "Gonzalez", "Perez", "Morales",
                                "Jimenez", "Ortiz",
                                "Diaz", "Castillo", "Navarro", "Ruiz", "Vega", "Guerrero", "Medina", "Vargas",
                                "Dominguez", "Soto",
                                "Estrada", "Padilla", "Salazar", "Aguilar", "Fuentes", "Rios", "Campos", "Espinoza",
                                "Cano", "Contreras",
                                "Miranda", "Lozano", "Molina", "Silva", "Ferrer", "Castro", "Romero", "Guzman", "Munoz",
                                "Moreno"
                };
                String[] genders = { "Male", "Female" };

                List<Patient> patients = new ArrayList<>();
                for (int i = 0; i < 150; i++) {
                        String first = firstNames[i % firstNames.length];
                        String last = lastNames[i % lastNames.length];
                        String fullName = first + " " + last;
                        String email = first.toLowerCase() + "." + last.toLowerCase().replace(" ", "") + i
                                        + "@example.com";
                        String phone = "09" + String.format("%09d", 200000000L + i);
                        String gender = genders[i % 2];
                        int age = 18 + (i % 60);

                        UserAccount user = userRepository.save(UserAccount.builder()
                                        .fullName(fullName)
                                        .email(email)
                                        .phone(phone)
                                        .passwordHash(passwordEncoderService.encode("Patient@1234"))
                                        .status("ACTIVE")
                                        .roles(Set.of(customerRole))
                                        .build());

                        Patient p = patientRepository.save(Patient.builder()
                                        .name(fullName)
                                        .phoneNo(phone)
                                        .age(age)
                                        .gender(gender)
                                        .username(email)
                                        .build());

                        patients.add(p);
                }

                log.info("[DataSeeder] Created {} patients", patients.size());

                // ── 7. Appointments (~400) ────────────────────────────────────────────
                ApprovalStatus[] statuses = {
                                ApprovalStatus.APPROVED, ApprovalStatus.APPROVED, ApprovalStatus.APPROVED,
                                ApprovalStatus.PENDING, ApprovalStatus.PENDING,
                                ApprovalStatus.REJECTED,
                                ApprovalStatus.CANCELLED
                };

                String[] remarks = {
                                "Annual check-up", "Follow-up consultation", "Pre-employment screening",
                                "Corporate medical exam", "Insurance physical exam", "Routine blood work",
                                "Post-surgery monitoring", "First-time check-up", "Doctor referral",
                                "Travel health requirement", "School physical requirement", "Pregnancy monitoring"
                };

                int appointmentCount = 0;
                for (int i = 0; i < 400; i++) {
                        Patient p = patients.get(i % patients.size());
                        DiagnosticCenter c = centers.get(i % centers.size());
                        ApprovalStatus status = statuses[i % statuses.length];

                        // Pick 1-3 tests from the center's test list
                        List<DiagnosticTest> centerTests = centerTestsMap.get(c);
                        int numTests = 1 + (i % Math.min(3, centerTests.size()));
                        Set<DiagnosticTest> chosenTests = Set.copyOf(centerTests.subList(0, numTests));

                        LocalDate date = switch (status) {
                                case PENDING -> LocalDate.now().plusDays(3 + (i % 30));
                                case APPROVED -> LocalDate.now().minusDays(1 + (i % 120));
                                case REJECTED -> LocalDate.now().minusDays(5 + (i % 60));
                                case CANCELLED -> LocalDate.now().minusDays(2 + (i % 45));
                        };

                        appointmentRepository.save(Appointment.builder()
                                        .patient(p)
                                        .diagnosticCenter(c)
                                        .diagnosticTests(chosenTests)
                                        .appointmentDate(date)
                                        .approvalStatus(status)
                                        .remarks(remarks[i % remarks.length])
                                        .build());

                        appointmentCount++;
                }

                log.info("[DataSeeder] Created {} appointments", appointmentCount);
                log.info("[DataSeeder] Seeding complete.");
        }

        // ── Helpers ───────────────────────────────────────────────────────────────

        private TestCategory cat(String name, String desc) {
                return TestCategory.builder().categoryName(name).description(desc).build();
        }

        private DiagnosticTest t(String name, double price, String normal, String units, TestCategory cat) {
                return DiagnosticTest.builder()
                                .testName(name)
                                .testPrice(price)
                                .normalValue(normal)
                                .units(units)
                                .status("ACTIVE")
                                .category(cat)
                                .build();
        }

        /** No-op helper — keeps inline list readable for repeated test references */
        private DiagnosticTest cholesterol(DiagnosticTest t) {
                return t;
        }
}
