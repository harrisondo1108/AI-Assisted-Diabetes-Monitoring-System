package com.quan.diabetes;

import com.quan.diabetes.entity.Role;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.entity.Room;
import com.quan.diabetes.repository.RoleRepository;
import com.quan.diabetes.repository.UserRepository;
import com.quan.diabetes.repository.RoomRepository;
import com.quan.diabetes.service.user.UserService;
import com.quan.diabetes.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component

public class AdminInitializer implements CommandLineRunner {
    @Autowired
    private UserRepository accountRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private MedicationRepository medicationRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private com.quan.diabetes.service.user.ProfileService profileService;
    @Autowired
    private com.quan.diabetes.service.user.PatientService patientService;
    @Autowired
    private com.quan.diabetes.service.user.PatientRoutineService patientRoutineService;

    private final String PHONE_NUMBER = "0328938692";
    @Override
    public void run(String... args) {
        // 1. Khởi tạo role REC nếu chưa có
        if (!roleRepository.existsById("REC")) {
            Role recRole = new Role();
            recRole.setRoleId("REC");
            recRole.setRoleName("Receptionist");
            roleRepository.save(recRole);
            System.out.println("Default REC role created.");
        }

        // 2. Khởi tạo admin nếu chưa có
        if (!accountRepository.existsByPhoneNumber(PHONE_NUMBER)) {
            Role adminRole = roleRepository.findById("AD")
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User admin = new User();
            admin.setUserId("AD000001");
            admin.setPhoneNumber(PHONE_NUMBER);
            admin.setPasswordHash("123456");
            admin.setRole(adminRole);

            userService.create(admin);
            System.out.println("Default admin account created.");
        }

        // 3. Khởi tạo lễ tân mặc định nếu chưa có
        String recPhone = "0328938699";
        if (!accountRepository.existsByPhoneNumber(recPhone)) {
            Role recRole = roleRepository.findById("REC").orElseThrow();
            User receptionist = new User();
            receptionist.setUserId("R000001");
            receptionist.setPhoneNumber(recPhone);
            receptionist.setPasswordHash("123456");
            receptionist.setRole(recRole);
            userService.create(receptionist);

            com.quan.diabetes.entity.Profile profile = new com.quan.diabetes.entity.Profile();
            profile.setUser(receptionist);
            profile.setFullName("Lễ tân hệ thống");
            profile.setPhoneNumber(recPhone);
            profileService.create(profile);
            System.out.println("Default receptionist account created.");
        }

        // 4. Khởi tạo bệnh nhân mặc định nếu chưa có
        String patPhone = "0328938691";
        if (!accountRepository.existsByPhoneNumber(patPhone)) {
            Role patRole = roleRepository.findById("PAT").orElseThrow();
            User patientUser = new User();
            patientUser.setUserId("P000001");
            patientUser.setPhoneNumber(patPhone);
            patientUser.setPasswordHash("123456");
            patientUser.setRole(patRole);
            userService.create(patientUser);

            com.quan.diabetes.entity.Patient patient = new com.quan.diabetes.entity.Patient();
            patient.setUserId("P000001");
            patient.setUser(patientUser);
            patient.setFullName("Bệnh nhân thử nghiệm");
            patient.setPhoneNumber(patPhone);
            patient.setDob(java.time.LocalDate.of(1995, 5, 15));
            patient.setGender(false); // Nam
            patient.setBloodgroup("O+");
            patient.setHeight(170);
            patient.setWeight(new java.math.BigDecimal("65.5"));
            patientService.create(patient);

            com.quan.diabetes.entity.PatientRoutine patientRoutine = new com.quan.diabetes.entity.PatientRoutine();
            patientRoutine.setPatient(patient);
            patientRoutineService.create(patientRoutine);
            System.out.println("Default patient account created.");
        }

        // 5. Khởi tạo danh sách bác sĩ mặc định nếu chưa có
        String[][] doctorData = {
            {"DOC00001", "0328938601", "BS. Nguyễn Văn A", "1980-05-15", "0", "Nội tiết - Tiểu đường", "123 Đường Giải Phóng, Hà Nội"},
            {"DOC00002", "0328938602", "BS. Trần Thị B", "1985-08-20", "1", "Nội tiết - Tiểu đường", "456 Đường Nguyễn Huệ, Quận 1, TP. HCM"},
            {"DOC00003", "0328938603", "BS. Phạm Minh C", "1990-12-10", "0", "Nội tiết - Tiểu đường", "789 Đường Lê Duẩn, Hải Châu, Đà Nẵng"},
            {"DOC00004", "0328938604", "BS. Nguyễn Văn D", "1992-03-25", "0", "Nội tiết - Tiểu đường", "321 Đường Lê Lợi, Huế"}
        };

        for (String[] doc : doctorData) {
            String docId = doc[0];
            String docPhone = doc[1];
            String docName = doc[2];
            java.time.LocalDate docDob = java.time.LocalDate.parse(doc[3]);
            boolean docGender = "1".equals(doc[4]);
            String docSpecialty = doc[5];
            String docAddress = doc[6];

            if (!accountRepository.existsByPhoneNumber(docPhone)) {
                Role docRole = roleRepository.findById("DOC").orElseThrow();
                User docUser = new User();
                docUser.setUserId(docId);
                docUser.setPhoneNumber(docPhone);
                docUser.setPasswordHash("123456");
                docUser.setRole(docRole);
                userService.create(docUser);

                com.quan.diabetes.entity.Profile docProfile = new com.quan.diabetes.entity.Profile();
                docProfile.setUser(docUser);
                docProfile.setFullName(docName);
                docProfile.setPhoneNumber(docPhone);
                docProfile.setDob(docDob);
                docProfile.setGender(docGender);
                docProfile.setSpecialty(docSpecialty);
                docProfile.setAddress(docAddress);

                // Gán phòng khám mặc định (Endocrinology Clinic)
                java.util.List<Room> rooms = roomRepository.findAll();
                if (!rooms.isEmpty()) {
                    docProfile.setRoom(rooms.get(0)); // Endocrinology Clinic là phòng đầu tiên
                }

                profileService.create(docProfile);
                System.out.println("Default doctor created: " + docName);
            }
        }

        // 6. Khởi tạo danh mục thuốc nếu chưa có
        if (medicationRepository.count() == 0) {
            String[][] medData = {
                {"MED001", "Metformin", "Viên nén", "500mg", "Đường uống", "Uống sau khi ăn"},
                {"MED002", "Gliclazide", "Viên nén", "30mg", "Đường uống", "Uống trước bữa ăn sáng"},
                {"MED003", "Insulin Glargine", "Bút tiêm", "100IU/ml", "Tiêm dưới da", "Tiêm vào cùng một thời điểm mỗi ngày"},
                {"MED004", "Sitagliptin", "Viên nén", "100mg", "Đường uống", "Uống một lần mỗi ngày"},
                {"MED005", "Tetracycline", "Viên nén", "250mg", "Đường uống", "Uống trước khi ăn 1 giờ hoặc sau khi ăn 2 giờ"},
                {"MED006", "Teflaro", "Bột pha tiêm", "600mg", "Truyền tĩnh mạch", "Sử dụng theo chỉ định của bác sĩ"}
            };
            for (String[] med : medData) {
                com.quan.diabetes.entity.Medication m = new com.quan.diabetes.entity.Medication();
                m.setMedicationId(med[0]);
                m.setMedicationName(med[1]);
                m.setForm(med[2]);
                m.setConcentration(med[3]);
                m.setAdministrationRoute(med[4]);
                m.setUsageInstruction(med[5]);
                m.setStatus("Active");
                medicationRepository.save(m);
            }
            System.out.println("Default medications catalog initialized.");
        }
    }
}