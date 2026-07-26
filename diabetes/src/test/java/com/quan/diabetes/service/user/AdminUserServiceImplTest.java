package com.quan.diabetes.service.user;

import com.quan.diabetes.dto.user.UserManagementDTO;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.entity.Role;
import com.quan.diabetes.entity.Room;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.ClinicalExaminationRepository;
import com.quan.diabetes.repository.PatientRepository;
import com.quan.diabetes.repository.ProfileRepository;
import com.quan.diabetes.repository.RoleRepository;
import com.quan.diabetes.repository.RoomRepository;
import com.quan.diabetes.repository.UserRepository;
import com.quan.diabetes.service.systemlog.SystemLogService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AdminUserServiceImpl.class})
class AdminUserServiceImplTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PatientRepository patientRepository;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private RoomRepository roomRepository;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ClinicalExaminationRepository clinicalExaminationRepository;

    @MockitoBean
    private SystemLogService systemLogService;

    @Autowired
    private AdminUserServiceImpl adminUserService;

    private User patUser;
    private User docUser;
    private User adminUser;
    private Patient patientEntity;
    private Profile profileEntity;
    private Role patRole;
    private Role docRole;
    private Role adminRole;
    private Room roomEntity;

    @BeforeEach
    void setUp() {
        patRole = new Role("PAT", "Patient");
        docRole = new Role("DOC", "Doctor");
        adminRole = new Role("ADM", "Admin");

        patUser = new User();
        patUser.setUserId("PAT001");
        patUser.setPhoneNumber("0987654321");
        patUser.setStatus(User.STATUS_ACTIVE);
        patUser.setRole(patRole);

        patientEntity = new Patient();
        patientEntity.setUserId("PAT001");
        patientEntity.setFullName("Nguyen Van A");
        patientEntity.setPhoneNumber("0987654321");
        patientEntity.setAddress("Hanoi");
        patientEntity.setDob(java.time.LocalDate.of(1990, 1, 1));
        patientEntity.setGender(false);
        patientEntity.setHeight(170);
        patientEntity.setWeight(java.math.BigDecimal.valueOf(65.0));
        patientEntity.setBloodgroup("A+");
        patientEntity.setPermanentMedicalHistory("Diabetes");
        patientEntity.setAllergyNotes("Penicillin");
        patientEntity.setSupervisorName("Nguyen Van B");
        patientEntity.setSupervisorPhone("0912345678");
        patientEntity.setEmail("pat@test.com");

        docUser = new User();
        docUser.setUserId("DOC001");
        docUser.setPhoneNumber("0912345678");
        docUser.setStatus(User.STATUS_ACTIVE);
        docUser.setRole(docRole);

        roomEntity = new Room("Room 101");
        profileEntity = new Profile();
        profileEntity.setUserId("DOC001");
        profileEntity.setFullName("Dr. Smith");
        profileEntity.setPhoneNumber("0912345678");
        profileEntity.setAddress("Hanoi Hospital");
        profileEntity.setDob(java.time.LocalDate.of(1980, 5, 5));
        profileEntity.setGender(false);
        profileEntity.setRoom(roomEntity);
        profileEntity.setSpecialty("Endocrinology");
        profileEntity.setEmail("doc@test.com");

        adminUser = new User();
        adminUser.setUserId("ADM001");
        adminUser.setPhoneNumber("0900000000");
        adminUser.setRole(adminRole);
    }

    @Test
    void testGetAllUserManagementDTOs_AllRoles_NoSearch() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(patUser, docUser, adminUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(patientEntity));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(profileEntity));

        List<UserManagementDTO> result = adminUserService.getAllUserManagementDTOs("all", null);

        assertNotNull(result);
        assertEquals(2, result.size());

        UserManagementDTO patDto = result.stream().filter(d -> d.getUserId().equals("PAT001")).findFirst().orElse(null);
        assertNotNull(patDto);
        assertEquals("Nguyen Van A", patDto.getFullName());
        assertEquals("0987654321", patDto.getAccountPhone());

        UserManagementDTO docDto = result.stream().filter(d -> d.getUserId().equals("DOC001")).findFirst().orElse(null);
        assertNotNull(docDto);
        assertEquals("Dr. Smith", docDto.getFullName());
        assertEquals("Room 101", docDto.getRoomName());
    }

    @Test
    void testGetAllUserManagementDTOs_EmptyRoleFilterAndWhitespaceSearch() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(patUser, docUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(patientEntity));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(profileEntity));

        List<UserManagementDTO> result = adminUserService.getAllUserManagementDTOs("", "   ");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetAllUserManagementDTOs_SearchByNameMatch() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(patUser, docUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(patientEntity));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(profileEntity));

        List<UserManagementDTO> result = adminUserService.getAllUserManagementDTOs("all", "Nguyen");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PAT001", result.get(0).getUserId());
    }

    @Test
    void testGetAllUserManagementDTOs_SearchByPhoneMatch() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(patUser, docUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(patientEntity));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(profileEntity));

        List<UserManagementDTO> result = adminUserService.getAllUserManagementDTOs("patient", "0987654321");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PAT001", result.get(0).getUserId());
    }

    @Test
    void testGetAllUserManagementDTOs_SearchNoMatch() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(patUser, docUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(patientEntity));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(profileEntity));

        List<UserManagementDTO> result = adminUserService.getAllUserManagementDTOs(null, "NonExistentTerm");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllUserManagementDTOs_DoctorAndPatientShortRoles() {
        User shortDoc = new User();
        shortDoc.setUserId("DOC002");
        shortDoc.setPhoneNumber("0911111111");
        shortDoc.setRole(new Role("DOC", "doc"));
        shortDoc.setStatus(User.STATUS_ACTIVE);

        User shortPat = new User();
        shortPat.setUserId("PAT002");
        shortPat.setPhoneNumber("0922222222");
        shortPat.setRole(new Role("PAT", "pat"));
        shortPat.setStatus(User.STATUS_ACTIVE);

        when(userRepository.findAll()).thenReturn(Arrays.asList(shortDoc, shortPat));
        when(patientRepository.findById("PAT002")).thenReturn(Optional.empty());
        when(profileRepository.findById("DOC002")).thenReturn(Optional.empty());

        List<UserManagementDTO> result = adminUserService.getAllUserManagementDTOs("all", null);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetAllUserManagementDTOs_RoleFilterMismatch() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(patUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(patientEntity));

        List<UserManagementDTO> result = adminUserService.getAllUserManagementDTOs("doctor", null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllUserManagementDTOs_NullRoleUser() {
        User nullRoleUser = new User();
        nullRoleUser.setUserId("NULL001");

        when(userRepository.findAll()).thenReturn(Collections.singletonList(nullRoleUser));

        List<UserManagementDTO> result = adminUserService.getAllUserManagementDTOs("all", null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPagedUserManagementDTOs_WithinRange() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(patUser, docUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(patientEntity));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(profileEntity));

        Page<UserManagementDTO> page = adminUserService.getPagedUserManagementDTOs("all", null, 0, 1);

        assertNotNull(page);
        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getContent().size());
    }

    @Test
    void testGetPagedUserManagementDTOs_OutOfBounds() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(patUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(patientEntity));

        Page<UserManagementDTO> page = adminUserService.getPagedUserManagementDTOs("all", null, 5, 10);

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void testCreateUserManagementDTO_Patient_RoleCreationInDB() {
        UserManagementDTO dto = new UserManagementDTO();
        dto.setRole("PAT");
        dto.setAccountPhone("0987654321");
        dto.setPassword("pass123");
        dto.setFullName("Nguyen Van A");

        when(userService.getNewID("PAT")).thenReturn("PAT001");
        when(roleRepository.findById("PAT")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userService.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(patientService.create(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        UserManagementDTO created = adminUserService.createUserManagementDTO(dto);

        assertNotNull(created);
        assertEquals("PAT001", created.getUserId());
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void testCreateUserManagementDTO_OtherRole_DefaultsToPatientRoleKey() {
        UserManagementDTO dto = new UserManagementDTO();
        dto.setRole("OTHER");
        dto.setAccountPhone("0987654321");

        when(userService.getNewID("OTHER")).thenReturn("PAT009");
        when(roleRepository.findById("PAT")).thenReturn(Optional.of(patRole));
        when(userService.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserManagementDTO created = adminUserService.createUserManagementDTO(dto);

        assertNotNull(created);
        assertEquals("PAT009", created.getUserId());
    }

    @Test
    void testCreateUserManagementDTO_Patient_NullFullName() {
        UserManagementDTO dto = new UserManagementDTO();
        dto.setRole("PAT");
        dto.setAccountPhone("0987654321");
        dto.setPassword("pass123");
        dto.setFullName(null);

        when(userService.getNewID("PAT")).thenReturn("PAT001");
        when(roleRepository.findById("PAT")).thenReturn(Optional.of(patRole));
        when(userService.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(patientService.create(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        UserManagementDTO created = adminUserService.createUserManagementDTO(dto);

        assertNotNull(created);
        assertEquals("PAT001", created.getUserId());
    }

    @Test
    void testCreateUserManagementDTO_Doctor_NewRoomCreation() {
        UserManagementDTO dto = new UserManagementDTO();
        dto.setRole("DOC");
        dto.setAccountPhone("0912345678");
        dto.setPassword("pass123");
        dto.setFullName("Dr. New");
        dto.setRoomName("New Room 102");

        when(userService.getNewID("DOC")).thenReturn("DOC002");
        when(roleRepository.findById("DOC")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userService.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roomRepository.findAll()).thenReturn(Collections.singletonList(roomEntity));
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserManagementDTO created = adminUserService.createUserManagementDTO(dto);

        assertNotNull(created);
        assertEquals("DOC002", created.getUserId());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void testCreateUserManagementDTO_Doctor_ExistingRoomReuse() {
        UserManagementDTO dto = new UserManagementDTO();
        dto.setRole("DOC");
        dto.setAccountPhone("0912345678");
        dto.setPassword("pass123");
        dto.setFullName("Dr. Smith");
        dto.setRoomName("Room 101");

        when(userService.getNewID("DOC")).thenReturn("DOC001");
        when(roleRepository.findById("DOC")).thenReturn(Optional.of(docRole));
        when(userService.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roomRepository.findAll()).thenReturn(Collections.singletonList(roomEntity));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserManagementDTO created = adminUserService.createUserManagementDTO(dto);

        assertNotNull(created);
        assertEquals("DOC001", created.getUserId());
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testCreateUserManagementDTO_NullRole() {
        UserManagementDTO dto = new UserManagementDTO();
        dto.setRole(null);
        dto.setAccountPhone("0912345678");

        when(userService.getNewID(null)).thenReturn("ID001");
        when(userService.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserManagementDTO created = adminUserService.createUserManagementDTO(dto);

        assertNotNull(created);
        assertEquals("ID001", created.getUserId());
    }

    @Test
    void testUpdateUserManagementDTO_Patient_Success_WithPassword() {
        UserManagementDTO inputDto = new UserManagementDTO();
        inputDto.setRole("PAT");
        inputDto.setAccountPhone("0987654321");
        inputDto.setPassword("newPassword123");
        inputDto.setFullName("Nguyen Van A Updated");

        when(userRepository.findById("PAT001")).thenReturn(Optional.of(patUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(patientEntity));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(patUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(patientEntity);

        UserManagementDTO updated = adminUserService.updateUserManagementDTO("PAT001", inputDto);

        assertNotNull(updated);
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(patientRepository, times(1)).save(patientEntity);
    }

    @Test
    void testUpdateUserManagementDTO_Patient_Success_EmptyPassword() {
        UserManagementDTO inputDto = new UserManagementDTO();
        inputDto.setRole("PAT");
        inputDto.setAccountPhone("0987654321");
        inputDto.setPassword("");

        when(userRepository.findById("PAT001")).thenReturn(Optional.of(patUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(patientEntity));
        when(userRepository.save(any(User.class))).thenReturn(patUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(patientEntity);

        UserManagementDTO updated = adminUserService.updateUserManagementDTO("PAT001", inputDto);

        assertNotNull(updated);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testUpdateUserManagementDTO_Patient_Success_NullFullName() {
        UserManagementDTO inputDto = new UserManagementDTO();
        inputDto.setRole("patient");
        inputDto.setAccountPhone("0987654321");
        inputDto.setFullName(null);

        when(userRepository.findById("PAT001")).thenReturn(Optional.of(patUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(patientEntity));
        when(userRepository.save(any(User.class))).thenReturn(patUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(patientEntity);

        UserManagementDTO updated = adminUserService.updateUserManagementDTO("PAT001", inputDto);

        assertNotNull(updated);
        assertEquals("Nguyen Van A", patientEntity.getFullName());
    }

    @Test
    void testUpdateUserManagementDTO_Doctor_Success_NewRoom() {
        UserManagementDTO inputDto = new UserManagementDTO();
        inputDto.setRole("DOC");
        inputDto.setAccountPhone("0912345678");
        inputDto.setFullName("Dr. Smith Updated");
        inputDto.setRoomName("New Room 202");

        when(userRepository.findById("DOC001")).thenReturn(Optional.of(docUser));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(profileEntity));
        when(roomRepository.findAll()).thenReturn(Collections.singletonList(roomEntity));
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenReturn(docUser);
        when(profileRepository.save(any(Profile.class))).thenReturn(profileEntity);

        UserManagementDTO updated = adminUserService.updateUserManagementDTO("DOC001", inputDto);

        assertNotNull(updated);
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void testUpdateUserManagementDTO_Doctor_Success_ExistingRoom() {
        UserManagementDTO inputDto = new UserManagementDTO();
        inputDto.setRole("DOC");
        inputDto.setAccountPhone("0912345678");
        inputDto.setRoomName("Room 101");

        when(userRepository.findById("DOC001")).thenReturn(Optional.of(docUser));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(profileEntity));
        when(roomRepository.findAll()).thenReturn(Collections.singletonList(roomEntity));
        when(userRepository.save(any(User.class))).thenReturn(docUser);
        when(profileRepository.save(any(Profile.class))).thenReturn(profileEntity);

        UserManagementDTO updated = adminUserService.updateUserManagementDTO("DOC001", inputDto);

        assertNotNull(updated);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testUpdateUserManagementDTO_UserNotFound() {
        UserManagementDTO inputDto = new UserManagementDTO();
        when(userRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            adminUserService.updateUserManagementDTO("UNKNOWN", inputDto);
        });

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void testUpdateUserManagementDTO_PatientRecordNotFound() {
        UserManagementDTO inputDto = new UserManagementDTO();
        inputDto.setRole("PAT");

        when(userRepository.findById("PAT001")).thenReturn(Optional.of(patUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            adminUserService.updateUserManagementDTO("PAT001", inputDto);
        });
    }

    @Test
    void testUpdateUserManagementDTO_ProfileRecordNotFound() {
        UserManagementDTO inputDto = new UserManagementDTO();
        inputDto.setRole("DOC");

        when(userRepository.findById("DOC001")).thenReturn(Optional.of(docUser));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            adminUserService.updateUserManagementDTO("DOC001", inputDto);
        });
    }

    @Test
    void testToggleLock_LockUser() {
        patUser.setStatus(User.STATUS_ACTIVE);
        when(userRepository.findById("PAT001")).thenReturn(Optional.of(patUser));
        when(userRepository.save(any(User.class))).thenReturn(patUser);

        adminUserService.toggleLock("PAT001");

        assertEquals(User.STATUS_LOCKED, patUser.getStatus());
    }

    @Test
    void testToggleLock_UnlockUser() {
        patUser.setStatus(User.STATUS_LOCKED);
        when(userRepository.findById("PAT001")).thenReturn(Optional.of(patUser));
        when(userRepository.save(any(User.class))).thenReturn(patUser);

        adminUserService.toggleLock("PAT001");

        assertEquals(User.STATUS_ACTIVE, patUser.getStatus());
    }

    @Test
    void testToggleLock_UserNotFound() {
        when(userRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> adminUserService.toggleLock("UNKNOWN"));
    }

    @Test
    void testGetUserManagementDTOById_PatientFound() {
        when(userRepository.findById("PAT001")).thenReturn(Optional.of(patUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(patientEntity));

        UserManagementDTO dto = adminUserService.getUserManagementDTOById("PAT001");

        assertNotNull(dto);
        assertEquals("PAT001", dto.getUserId());
        assertEquals("Nguyen Van A", dto.getFullName());
    }

    @Test
    void testGetUserManagementDTOById_PatientNotFoundInRepo() {
        when(userRepository.findById("PAT001")).thenReturn(Optional.of(patUser));
        when(patientRepository.findById("PAT001")).thenReturn(Optional.empty());

        UserManagementDTO dto = adminUserService.getUserManagementDTOById("PAT001");

        assertNotNull(dto);
        assertEquals("PAT001", dto.getUserId());
        assertNull(dto.getFullName());
    }

    @Test
    void testGetUserManagementDTOById_DoctorWithRoom() {
        when(userRepository.findById("DOC001")).thenReturn(Optional.of(docUser));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(profileEntity));

        UserManagementDTO dto = adminUserService.getUserManagementDTOById("DOC001");

        assertNotNull(dto);
        assertEquals("DOC001", dto.getUserId());
        assertEquals("Room 101", dto.getRoomName());
    }

    @Test
    void testGetUserManagementDTOById_DoctorNoRoom() {
        Profile noRoomProfile = new Profile();
        noRoomProfile.setUserId("DOC001");
        noRoomProfile.setFullName("Dr. No Room");

        when(userRepository.findById("DOC001")).thenReturn(Optional.of(docUser));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(noRoomProfile));

        UserManagementDTO dto = adminUserService.getUserManagementDTOById("DOC001");

        assertNotNull(dto);
        assertEquals("DOC001", dto.getUserId());
        assertNull(dto.getRoomName());
    }

    @Test
    void testGetUserManagementDTOById_DoctorNotFoundInRepo() {
        when(userRepository.findById("DOC001")).thenReturn(Optional.of(docUser));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.empty());

        UserManagementDTO dto = adminUserService.getUserManagementDTOById("DOC001");

        assertNotNull(dto);
        assertEquals("DOC001", dto.getUserId());
        assertNull(dto.getFullName());
    }

    @Test
    void testGetUserManagementDTOById_NullRoleUser() {
        User nullRoleUser = new User();
        nullRoleUser.setUserId("NULL001");
        nullRoleUser.setRole(null);

        when(userRepository.findById("NULL001")).thenReturn(Optional.of(nullRoleUser));

        UserManagementDTO dto = adminUserService.getUserManagementDTOById("NULL001");

        assertNotNull(dto);
        assertEquals("NULL001", dto.getUserId());
        assertNull(dto.getRole());
    }

    @Test
    void testGetUserManagementDTOById_UserNotFound() {
        when(userRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> adminUserService.getUserManagementDTOById("UNKNOWN"));
    }

    @Test
    void testIsPhoneTaken_NullOrBlankPhone() {
        assertFalse(adminUserService.isPhoneTaken(null, null));
        assertFalse(adminUserService.isPhoneTaken("   ", "PAT001"));
    }

    @Test
    void testIsPhoneTaken_PhoneNotFound() {
        when(userRepository.findByPhoneNumber("0987654321")).thenReturn(Optional.empty());

        assertFalse(adminUserService.isPhoneTaken("0987654321", null));
    }

    @Test
    void testIsPhoneTaken_PhoneFound_NoExcludeUser() {
        when(userRepository.findByPhoneNumber("0987654321")).thenReturn(Optional.of(patUser));

        assertTrue(adminUserService.isPhoneTaken("0987654321", null));
        assertTrue(adminUserService.isPhoneTaken("0987654321", "   "));
    }

    @Test
    void testIsPhoneTaken_PhoneFound_SameUser() {
        when(userRepository.findByPhoneNumber("0987654321")).thenReturn(Optional.of(patUser));

        assertFalse(adminUserService.isPhoneTaken("0987654321", "PAT001"));
    }

    @Test
    void testIsPhoneTaken_PhoneFound_DifferentUser() {
        when(userRepository.findByPhoneNumber("0987654321")).thenReturn(Optional.of(patUser));

        assertTrue(adminUserService.isPhoneTaken("0987654321", "OTHER001"));
    }

    // ────────────────────────────────────────────────────────────────────────
    // Extra tests to cover remaining JaCoCo missed branches
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Line 131 branch: getAllUserManagementDTOs — Doctor profile exists but room == null
     * (inner branch of "if (p.getRoom() != null)" is the FALSE path, i.e. room IS null)
     */
    @Test
    void testGetAllUserManagementDTOs_DoctorProfileWithNullRoom() {
        Profile noRoomProfile = new Profile();
        noRoomProfile.setUserId("DOC001");
        noRoomProfile.setFullName("Dr. No Room");
        noRoomProfile.setRoom(null); // room is null → branch not taken

        when(userRepository.findAll()).thenReturn(Collections.singletonList(docUser));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(noRoomProfile));

        List<UserManagementDTO> result = adminUserService.getAllUserManagementDTOs("all", null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getRoomName());
    }

    /**
     * Line 225 branch: createUserManagementDTO — Doctor created with roomName = null
     * (skips the room-mapping block entirely)
     */
    @Test
    void testCreateUserManagementDTO_Doctor_NullRoomName() {
        UserManagementDTO dto = new UserManagementDTO();
        dto.setRole("DOC");
        dto.setAccountPhone("0912345678");
        dto.setPassword("pass123");
        dto.setFullName("Dr. No Room");
        dto.setRoomName(null); // null → room block skipped

        when(userService.getNewID("DOC")).thenReturn("DOC003");
        when(roleRepository.findById("DOC")).thenReturn(Optional.of(docRole));
        when(userService.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserManagementDTO created = adminUserService.createUserManagementDTO(dto);

        assertNotNull(created);
        assertEquals("DOC003", created.getUserId());
        verify(roomRepository, never()).findAll();
    }

    /**
     * Line 247 branch: updateUserManagementDTO — orElseThrow lambda on the SECOND
     * userRepository.findById call is hit.
     * getUserManagementDTOById (line 245) finds the user on the 1st call;
     * userRepository.findById on line 247 returns empty on the 2nd call → throws.
     */
    @Test
    void testUpdateUserManagementDTO_UserFoundForOldDtoButNotForUpdate() {
        UserManagementDTO inputDto = new UserManagementDTO();
        inputDto.setRole("PAT");
        inputDto.setAccountPhone("0987654321");

        // First call (from getUserManagementDTOById inside updateUserManagementDTO) → success
        // Second call (line 247) → empty → triggers orElseThrow lambda
        when(userRepository.findById("PAT001"))
                .thenReturn(Optional.of(patUser))   // 1st call: used by getUserManagementDTOById
                .thenReturn(Optional.empty());       // 2nd call: line 247 → throws

        // getUserManagementDTOById also calls patientRepository
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(patientEntity));

        assertThrows(EntityNotFoundException.class, () ->
                adminUserService.updateUserManagementDTO("PAT001", inputDto));
    }

    /**
     * Line 254 branch: updateUserManagementDTO — dto.getRole() == null → goes to
     * else/profile branch (not null-PAT branch).
     */
    @Test
    void testUpdateUserManagementDTO_NullRole_GoesToProfileBranch() {
        UserManagementDTO inputDto = new UserManagementDTO();
        inputDto.setRole(null); // null role → condition (role != null && ...) is false → else branch
        inputDto.setAccountPhone("0912345678");
        inputDto.setRoomName(null); // also covers null-roomName in update

        // getUserManagementDTOById is called first on line 245 for docUser (null role user)
        // We'll use adminUser (role=ADM) so it goes to profile branch in getUserManagementDTOById too
        User nullRoleUser = new User();
        nullRoleUser.setUserId("ADM001");
        nullRoleUser.setPhoneNumber("0900000000");
        nullRoleUser.setRole(null);

        when(userRepository.findById("ADM001"))
                .thenReturn(Optional.of(nullRoleUser)); // both calls return the same user
        when(profileRepository.findById("ADM001")).thenReturn(Optional.of(profileEntity));
        when(userRepository.save(any(User.class))).thenReturn(nullRoleUser);
        when(profileRepository.save(any(Profile.class))).thenReturn(profileEntity);

        UserManagementDTO result = adminUserService.updateUserManagementDTO("ADM001", inputDto);

        assertNotNull(result);
        verify(profileRepository, atLeastOnce()).findById("ADM001");
    }

    /**
     * Line 283 branch: updateUserManagementDTO — Doctor updated with roomName = null
     * (skips the room-mapping block in the else/profile branch)
     */
    @Test
    void testUpdateUserManagementDTO_Doctor_NullRoomName() {
        UserManagementDTO inputDto = new UserManagementDTO();
        inputDto.setRole("DOC");
        inputDto.setAccountPhone("0912345678");
        inputDto.setRoomName(null); // null → room block skipped (line 283 false branch)

        when(userRepository.findById("DOC001"))
                .thenReturn(Optional.of(docUser));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(profileEntity));
        when(userRepository.save(any(User.class))).thenReturn(docUser);
        when(profileRepository.save(any(Profile.class))).thenReturn(profileEntity);

        UserManagementDTO result = adminUserService.updateUserManagementDTO("DOC001", inputDto);

        assertNotNull(result);
        verify(roomRepository, never()).findAll();
    }

    /**
     * Line 225 branch: createUserManagementDTO — Doctor created with roomName = "   " (blank string)
     * roomName != null BUT isBlank() == true → room block skipped (A=true, B=false branch)
     */
    @Test
    void testCreateUserManagementDTO_Doctor_BlankRoomName() {
        UserManagementDTO dto = new UserManagementDTO();
        dto.setRole("DOC");
        dto.setAccountPhone("0912345678");
        dto.setPassword("pass123");
        dto.setFullName("Dr. Blank Room");
        dto.setRoomName("   "); // not null but blank → isBlank() true → skips room block

        when(userService.getNewID("DOC")).thenReturn("DOC004");
        when(roleRepository.findById("DOC")).thenReturn(Optional.of(docRole));
        when(userService.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserManagementDTO created = adminUserService.createUserManagementDTO(dto);

        assertNotNull(created);
        assertEquals("DOC004", created.getUserId());
        verify(roomRepository, never()).findAll();
    }

    /**
     * Line 283 branch: updateUserManagementDTO — Doctor updated with roomName = "   " (blank string)
     * roomName != null BUT isBlank() == true → room block skipped (A=true, B=false branch)
     */
    @Test
    void testUpdateUserManagementDTO_Doctor_BlankRoomName() {
        UserManagementDTO inputDto = new UserManagementDTO();
        inputDto.setRole("DOC");
        inputDto.setAccountPhone("0912345678");
        inputDto.setRoomName("   "); // not null but blank → isBlank() true → skips room block

        when(userRepository.findById("DOC001"))
                .thenReturn(Optional.of(docUser));
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(profileEntity));
        when(userRepository.save(any(User.class))).thenReturn(docUser);
        when(profileRepository.save(any(Profile.class))).thenReturn(profileEntity);

        UserManagementDTO result = adminUserService.updateUserManagementDTO("DOC001", inputDto);

        assertNotNull(result);
        verify(roomRepository, never()).findAll();
    }
}

