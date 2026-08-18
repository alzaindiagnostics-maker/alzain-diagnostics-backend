package com.alzain.config;

import com.alzain.entity.BusinessSetting;
import com.alzain.entity.PackageItem;
import com.alzain.entity.TestItem;
import com.alzain.entity.User;
import com.alzain.repository.BusinessSettingRepository;
import com.alzain.repository.PackageRepository;
import com.alzain.repository.TestRepository;
import com.alzain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final TestRepository testRepository;
    private final BusinessSettingRepository businessSettingRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.initial.username:admin}")
    private String adminUsername;

    @Value("${admin.initial.password:admin123}")
    private String adminPassword;

    @Value("${admin.initial.email:alzaindiagnostics@gmail.com}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedAdminUser();
        seedBusinessSettings();
        seedPackagesAndTests();
    }

    private void seedAdminUser() {
        Optional<User> existingUser = userRepository.findByUsername(adminUsername);
        if (existingUser.isEmpty()) {
            existingUser = userRepository.findByEmail(adminEmail);
        }

        if (existingUser.isPresent()) {
            User admin = existingUser.get();
            admin.setEmail(adminEmail);
            admin.setRole("ROLE_ADMIN");
            if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode(adminPassword));
                log.info("Admin password reset to default seed password hash: {}", adminUsername);
            }
            userRepository.save(admin);
            log.info("Verified active admin account in MySQL: username={}, email={}, role=ROLE_ADMIN", admin.getUsername(), admin.getEmail());
        } else {
            User admin = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .email(adminEmail)
                    .role("ROLE_ADMIN")
                    .build();
            userRepository.save(admin);
            log.info("Initial admin user created successfully: username={}, email={}, role=ROLE_ADMIN", adminUsername, adminEmail);
        }
    }

    private void seedBusinessSettings() {
        createSettingIfAbsent("business_name", "AL-ZAIN DIAGNOSTICS", "Laboratory Business Name");
        createSettingIfAbsent("business_tagline", "ACCURATE | RELIABLE | TRUSTED", "Business Tagline");
        createSettingIfAbsent("business_address", "Rajampet Road, Near V.M. Hospital, Pullampet, Andhra Pradesh - 516107", "Laboratory Address");
        createSettingIfAbsent("primary_phone", "+918374874335", "Primary Contact Number");
        createSettingIfAbsent("secondary_phone", "+919949963552", "Secondary Contact Number");
        createSettingIfAbsent("whatsapp_number", "+918374874335", "WhatsApp Contact Number");
        createSettingIfAbsent("email", "alzaindiagnostics@gmail.com", "Business Email Address");
        createSettingIfAbsent("website", "www.alzaindiagnostics.com", "Official Website");
        createSettingIfAbsent("instagram", "AL_ZAIN_DIAGNOSTICS", "Instagram Handle");
    }

    private void createSettingIfAbsent(String key, String value, String desc) {
        if (businessSettingRepository.findBySettingKey(key).isEmpty()) {
            businessSettingRepository.save(BusinessSetting.builder()
                    .settingKey(key)
                    .settingValue(value)
                    .description(desc)
                    .build());
        }
    }

    private void seedPackagesAndTests() {
        if (packageRepository.count() > 0) {
            return; // Seed data already loaded
        }

        log.info("Seeding initial diagnostic packages and tests into MySQL...");

        // 1. GULF MEDICAL PACKAGE
        packageRepository.save(PackageItem.builder()
                .name("GULF MEDICAL PACKAGE")
                .slug("gulf-medical-package")
                .shortDescription("Comprehensive health screening package designed for employment and Gulf/visa-related medical examinations.")
                .detailedDescription("The Gulf Medical Package is meticulously designed to meet diagnostic requirements for overseas employment and visa medical clearance. It includes key blood, organ function, and infectious disease screenings.")
                .originalPrice(null)
                .offerPrice(1199.0)
                .discountPercentage(null)
                .category("Medical / Gulf")
                .featured(true)
                .active(true)
                .homeCollectionAvailable(true)
                .parametersText("14 Tests Included")
                .preparationInstructions("10-12 hours overnight fasting required for Fasting Blood Sugar (FBS) test.")
                .reportInformation("Reports generated within 24-48 hours. Digital PDF copy available on request.")
                .testNames(Arrays.asList("CBP / CBC", "ESR", "Blood Grouping", "VDRL", "HIV 1 & 2", "HBsAg", "HCV", "TPHA", "FBS", "PPBS", "KFT", "Lipid Profile", "LFT", "Urine Analysis"))
                .build());

        // 2. SPECIAL HEALTH PACKAGE
        packageRepository.save(PackageItem.builder()
                .name("SPECIAL HEALTH PACKAGE")
                .slug("special-health-package")
                .shortDescription("Comprehensive total body health screening covering major organ functions, metabolic markers, and thyroid.")
                .detailedDescription("An all-in-one general health package providing an in-depth review of your body's major organ systems including Liver, Kidney, Lipid, Thyroid, and Blood Glucose status.")
                .originalPrice(null)
                .offerPrice(1499.0)
                .discountPercentage(null)
                .category("Health Checkup")
                .featured(true)
                .active(true)
                .homeCollectionAvailable(true)
                .parametersText("14 Comprehensive Tests")
                .preparationInstructions("Fasting of 10-12 hours recommended prior to sample collection.")
                .reportInformation("Same day or next morning report delivery.")
                .testNames(Arrays.asList("CBP / CBC", "ESR", "Blood Grouping", "HIV 1 & 2", "HBsAg", "HCV", "VDRL", "FBS", "PPBS", "KFT", "Lipid Profile", "LFT", "Urine Analysis", "Thyroid Profile"))
                .build());

        // 3. COMPLETE HEALTH CHECKUP
        packageRepository.save(PackageItem.builder()
                .name("COMPLETE HEALTH CHECKUP")
                .slug("complete-health-checkup")
                .shortDescription("Our flagship diagnostic package featuring 13 Test Profiles and 92+ Parameters for thorough health monitoring.")
                .detailedDescription("The most extensive health package offered at AL-ZAIN Diagnostics. Covers Diabetes, Thyroid, Essential Vitamins (B12 & D3), Liver, Kidney, Blood Health, and Urine routine analysis.")
                .originalPrice(4999.0)
                .offerPrice(2099.0)
                .discountPercentage(58)
                .category("Health Checkup")
                .featured(true)
                .active(true)
                .homeCollectionAvailable(true)
                .parametersText("13 Test Profiles | 92+ Parameters")
                .preparationInstructions("10-12 hours fasting required. Avoid heavy vitamin supplements 24 hours prior.")
                .reportInformation("Reports provided within 24 hours.")
                .testNames(Arrays.asList("Diabetes Screening (Fasting Glucose, HbA1c)", "Thyroid Profile (T3, T4, TSH)", "Vitamins (Vitamin B12, Vitamin D3)", "Liver Profile", "Kidney Profile", "Blood Health (CBC, ESR, Iron Profile)", "Urine Examination Routine"))
                .build());

        // 4. DIABETES PROFILE
        packageRepository.save(PackageItem.builder()
                .name("DIABETES PROFILE")
                .slug("diabetes-profile")
                .shortDescription("Specialized monitoring package for blood sugar evaluation and long-term glycemic control (HbA1c).")
                .detailedDescription("Essential test panel for diabetic patients and individuals experiencing symptoms of diabetes. Measures average 3-month blood sugar (HbA1c), fasting glucose, post-meal glucose, and urine parameters.")
                .originalPrice(949.0)
                .offerPrice(699.0)
                .discountPercentage(26)
                .category("Diabetes")
                .featured(false)
                .active(true)
                .homeCollectionAvailable(true)
                .parametersText("4 Essential Tests")
                .preparationInstructions("10-12 hours overnight fasting required. Post-Prandial test taken 2 hours after breakfast.")
                .reportInformation("Same-day reports.")
                .testNames(Arrays.asList("HbA1c", "Fasting Blood Sugar", "Post-Prandial Blood Sugar", "Urine Analysis"))
                .build());

        // 5. FEVER PROFILE
        packageRepository.save(PackageItem.builder()
                .name("FEVER PROFILE")
                .slug("fever-profile")
                .shortDescription("Comprehensive diagnostic panel for diagnosing acute and persistent fevers including Dengue, Typhoid, and Malaria.")
                .detailedDescription("Designed for fast identification of infectious fever triggers. Includes serology tests for Dengue (NS1, IgG, IgM), Typhoid (Widal), Malaria Parasite, CRP inflammatory marker, CBC, and Liver Bilirubin.")
                .originalPrice(1649.0)
                .offerPrice(1049.0)
                .discountPercentage(36)
                .category("Fever")
                .featured(true)
                .active(true)
                .homeCollectionAvailable(true)
                .parametersText("10 Diagnostic Tests")
                .preparationInstructions("No fasting required. Inform technician about current fever medications.")
                .reportInformation("Express same-day reports.")
                .testNames(Arrays.asList("CBC", "ESR", "Malaria Parasite", "Widal Test", "C-Reactive Protein (CRP)", "Dengue NS1 Antigen", "Dengue IgG", "Dengue IgM", "Serum Bilirubin", "Urine Analysis"))
                .build());

        // 6. FEVER PROFILE - BASIC
        packageRepository.save(PackageItem.builder()
                .name("FEVER PROFILE - BASIC")
                .slug("fever-profile-basic")
                .shortDescription("Essential fever screening panel for initial evaluation of seasonal fevers and blood cell counts.")
                .detailedDescription("A focused budget fever package covering Complete Blood Count, ESR, Malaria parasite detection, Widal typhoid test, CRP, and Urine Routine.")
                .originalPrice(999.0)
                .offerPrice(549.0)
                .discountPercentage(45)
                .category("Fever")
                .featured(false)
                .active(true)
                .homeCollectionAvailable(true)
                .parametersText("6 Essential Tests")
                .preparationInstructions("No fasting required.")
                .reportInformation("Same-day delivery.")
                .testNames(Arrays.asList("CBC", "ESR", "Malaria Parasite", "Widal Test", "C-Reactive Protein (CRP)", "Urine Analysis"))
                .build());

        // 7. LIVER FUNCTION TEST
        packageRepository.save(PackageItem.builder()
                .name("LIVER FUNCTION TEST")
                .slug("liver-function-test")
                .shortDescription("Biochemical test panel assessing liver enzyme levels, protein synthesis, and biliary system health.")
                .detailedDescription("Measures key liver enzymes (SGOT, SGPT, ALP), Bilirubin levels, Total Protein, Albumin, Globulin, and A/G Ratio to check for hepatic inflammation or liver disease.")
                .originalPrice(600.0)
                .offerPrice(299.0)
                .discountPercentage(50)
                .category("Liver")
                .featured(false)
                .active(true)
                .homeCollectionAvailable(true)
                .parametersText("8 Parameters")
                .preparationInstructions("8-10 hours fasting recommended. Avoid alcohol 24 hours prior.")
                .reportInformation("Same-day reports.")
                .testNames(Arrays.asList("Serum Bilirubin", "SGOT / AST", "SGPT / ALT", "ALP", "Serum Protein", "Albumin", "Globulin", "A/G Ratio"))
                .build());

        // 8. LIPID PROFILE
        packageRepository.save(PackageItem.builder()
                .name("LIPID PROFILE")
                .slug("lipid-profile")
                .shortDescription("Complete lipid panel measuring blood cholesterol and triglycerides for cardiovascular wellness.")
                .detailedDescription("Assesses risk of coronary heart disease by calculating Total Cholesterol, High-Density Lipoprotein (HDL), Low-Density Lipoprotein (LDL), VLDL, and Triglycerides.")
                .originalPrice(500.0)
                .offerPrice(249.0)
                .discountPercentage(50)
                .category("Lipid")
                .featured(false)
                .active(true)
                .homeCollectionAvailable(true)
                .parametersText("5 Parameters")
                .preparationInstructions("10-12 hours strict overnight fasting required for accurate triglyceride results.")
                .reportInformation("Same-day delivery.")
                .testNames(Arrays.asList("Total Cholesterol", "HDL", "LDL", "VLDL", "Serum Triglycerides"))
                .build());

        // 9. ELECTROLYTES PROFILE
        packageRepository.save(PackageItem.builder()
                .name("ELECTROLYTES PROFILE")
                .slug("electrolytes-profile")
                .shortDescription("Serum electrolyte assay measuring essential blood minerals for kidney, nerve, and cardiac function.")
                .detailedDescription("Evaluates key electrolytes essential for cellular fluid homeostasis and neuromuscular function, including Sodium, Potassium, Calcium, and Phosphorus.")
                .originalPrice(900.0)
                .offerPrice(499.0)
                .discountPercentage(45)
                .category("Electrolytes")
                .featured(false)
                .active(true)
                .homeCollectionAvailable(true)
                .parametersText("4 Mineral Parameters")
                .preparationInstructions("No strict fasting required unless requested by physician.")
                .reportInformation("Same-day delivery.")
                .testNames(Arrays.asList("Sodium (Na+)", "Potassium (K+)", "Serum Calcium", "Phosphorus"))
                .build());

        // 10. THYROID PROFILE
        packageRepository.save(PackageItem.builder()
                .name("THYROID PROFILE")
                .slug("thyroid-profile")
                .shortDescription("Triple hormone blood assay (T3, T4, TSH) to evaluate thyroid gland health and metabolic regulation.")
                .detailedDescription("Checks for hypothyroidism or hyperthyroidism by measuring circulating levels of Triiodothyronine (T3), Thyroxine (T4), and Thyroid-Stimulating Hormone (TSH).")
                .originalPrice(null)
                .offerPrice(399.0)
                .discountPercentage(null)
                .category("Thyroid")
                .featured(false)
                .active(true)
                .homeCollectionAvailable(true)
                .parametersText("3 Key Hormones")
                .preparationInstructions("Fasting not mandatory, but morning sample collection recommended before thyroid medications.")
                .reportInformation("Same-day delivery.")
                .testNames(Arrays.asList("T3 - Triiodothyronine", "T4 - Thyroxine", "TSH - Thyroid-Stimulating Hormone"))
                .build());

        log.info("Seeded 10 initial packages successfully into database.");
    }
}
