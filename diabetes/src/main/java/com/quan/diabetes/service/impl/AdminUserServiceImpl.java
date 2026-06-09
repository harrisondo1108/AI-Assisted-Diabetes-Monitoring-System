package com.quan.diabetes.service.impl;

import com.quan.diabetes.dto.UserManagementDTO;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.entity.Role;
import com.quan.diabetes.entity.Room;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.PatientRepository;
import com.quan.diabetes.repository.ProfileRepository;
import com.quan.diabetes.repository.RoleRepository;
import com.quan.diabetes.repository.RoomRepository;
import com.quan.diabetes.repository.UserRepository;
import com.quan.diabetes.service.AdminUserService;
import com.quan.diabetes.service.PatientService;
import com.quan.diabetes.service.UserService;
import com.quan.diabetes.util.ParseUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Service implementation for admin‑level user management.
 * It aggregates data from multiple domain entities (User, Patient, Profile, Role)
 * and exposes simple DTOs for the front‑end.
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final RoomRepository roomRepository;
    private final UserService userService;
    private final PatientService patientService;
    private final PasswordEncoder passwordEncoder;
    public AdminUserServiceImpl(UserRepository userRepository,
                                PatientRepository patientRepository,
                                ProfileRepository profileRepository,
                                RoleRepository roleRepository,
                                RoomRepository roomRepository,
                                UserService userService,
                                PatientService patientService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.profileRepository = profileRepository;
        this.roleRepository = roleRepository;
        this.roomRepository = roomRepository;
        this.userService = userService;
        this.patientService = patientService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserManagementDTO> getAllUserManagementDTOs(String role, String search) {
        List<User> users = userRepository.findAll();
        List<UserManagementDTO> dtos = new ArrayList<>();

        final String searchLower = search != null ? search.toLowerCase() : "";
        final String roleFilter = role != null && !role.isEmpty() && !role.equalsIgnoreCase("all") ? role.toLowerCase() : null;

        for (User u : users) {
            String uRole = u.getRole() != null ? u.getRole().getRoleName().toLowerCase() : "";

            // Filter by role early
            if (roleFilter != null && !uRole.contains(roleFilter)) {
                continue;
            }

            UserManagementDTO dto = new UserManagementDTO();
            dto.setUserId(u.getUserId());
            dto.setAccountPhone(u.getPhoneNumber());
            dto.setStatus(u.getStatus());
            dto.setRole(uRole);

            // Populate patient‑specific fields
            if (uRole.contains("patient")) {
                patientRepository.findById(u.getUserId()).ifPresent(p -> {
                    dto.setFullName(p.getFullName());
                    dto.setAccountPhone(p.getPhoneNumber());
                    dto.setAddress(p.getAddress());
                    dto.setDob(p.getDob());
                    dto.setGender(p.getGender());
                    dto.setHeight(p.getHeight());
                    dto.setWeight(p.getWeight());
                    dto.setBloodgroup(p.getBloodgroup());
                    dto.setPermanentMedicalHistory(p.getPermanentMedicalHistory());
                    dto.setAllergyNotes(p.getAllergyNotes());
                    dto.setSupervisorName(p.getSupervisorName());
                    dto.setSupervisorPhone(p.getSupervisorPhone());
                });
            } else { // profile (doctor) fields
                profileRepository.findById(u.getUserId()).ifPresent(p -> {
                    dto.setFullName(p.getFullName());
                    dto.setAccountPhone(p.getPhoneNumber());
                    dto.setAddress(p.getAddress());
                    dto.setDob(p.getDob());
                    dto.setGender(p.getGender());
                    if (p.getRoom() != null) {
                        dto.setRoomName(p.getRoom().getRoomName());
                    }
                    dto.setSpecialty(p.getSpecialty());
                });
            }

            // Filter by search query (name, phone, accountPhone)
            if (!searchLower.isEmpty()) {
                String fullName = dto.getFullName() != null ? dto.getFullName().toLowerCase() : "";
                String phone = dto.getAccountPhone() != null ? dto.getAccountPhone().toLowerCase() : "";
                String accPhone = dto.getAccountPhone() != null ? dto.getAccountPhone().toLowerCase() : "";
                if (!fullName.contains(searchLower) && !phone.contains(searchLower) && !accPhone.contains(searchLower)) {
                    continue;
                }
            }

            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementDTO> getPagedUserManagementDTOs(String role, String search, int page, int size) {
        List<UserManagementDTO> all = getAllUserManagementDTOs(role, search);
        int total = all.size();
        int from = page * size;
        if (from >= total) {
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), total);
        }
        int to = Math.min(from + size, total);
        List<UserManagementDTO> content = all.subList(from, to);
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    @Transactional
    public UserManagementDTO createUserManagementDTO(UserManagementDTO dto) {
        // 1. Create User entity
        User user = new User();
        System.out.println(dto.getRole());
        user.setUserId(userService.getNewID(dto.getRole()));
        user.setPhoneNumber(dto.getAccountPhone());
        user.setPasswordHash(dto.getPassword());


        // 2. Resolve (or create) Role entity
        Role role = null;
        if (dto.getRole() != null) {
            String roleKey = dto.getRole().equals("DOC") ? "DOC" : "PAT";
            String roleName = roleKey.equals("DOC") ? "Doctor" : "Patient";
            role = roleRepository.findById(roleKey).orElseGet(() -> {
                Role newRole = new Role(roleKey, roleName);
                return roleRepository.save(newRole);
            });
        }
        user.setRole(role);
        user = userService.create(user);

        // 3. Persist Patient or Profile depending on role
        if (dto.getRole() != null && dto.getRole().equals("PAT")) {
            Patient p = new Patient();
//            p.setUserId(user.getUserId());
            p.setUser(user);
            p.setFullName(dto.getFullName() != null ? dto.getFullName() : "Unknown");
            p.setPhoneNumber(dto.getAccountPhone());
            p.setAddress(ParseUtil.parseString(dto.getAddress()));
            p.setDob(dto.getDob());
            p.setGender(dto.getGender());
            p.setHeight(dto.getHeight());
            p.setWeight(dto.getWeight());
            p.setBloodgroup(ParseUtil.parseString(dto.getBloodgroup()));
            p.setPermanentMedicalHistory(ParseUtil.parseString(dto.getPermanentMedicalHistory()));
            p.setAllergyNotes(ParseUtil.parseString(dto.getAllergyNotes()));
            p.setSupervisorName(ParseUtil.parseString(dto.getSupervisorName()));
            p.setSupervisorPhone(ParseUtil.parseString(dto.getSupervisorPhone()));

            p = patientService.create(p);

        } else {
            Profile p = new Profile();
//            p.setUserId(user.getUserId());
            p.setUser(user);
            p.setFullName(dto.getFullName() != null ? dto.getFullName() : "Unknown");
            p.setPhoneNumber(dto.getAccountPhone());
            p.setAddress(ParseUtil.parseString(dto.getAddress()));
            p.setDob(dto.getDob());
            p.setGender(dto.getGender());
            p.setSpecialty(ParseUtil.parseString(dto.getSpecialty()));
            // Ánh xạ phòng khám
            if (dto.getRoomName() != null && !dto.getRoomName().isBlank()) {
                Room room = roomRepository.findAll().stream()
                        .filter(r -> r.getRoomName().equalsIgnoreCase(dto.getRoomName()))
                        .findFirst()
                        .orElseGet(() -> roomRepository.save(new Room(dto.getRoomName())));
                p.setRoom(room);
            }
            profileRepository.save(p);
        }
        dto.setUserId(user.getUserId());
        return dto;
    }

    @Override
    @Transactional
    public UserManagementDTO updateUserManagementDTO(String userId, UserManagementDTO dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setPhoneNumber(dto.getAccountPhone());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPasswordHash(dto.getPassword());
        }
        userRepository.save(user);

        if (dto.getRole() != null && (dto.getRole().equalsIgnoreCase("PAT") || dto.getRole().equalsIgnoreCase("patient"))) {
            Patient p = patientRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Patient record not found for user: " + userId));
            p.setFullName(dto.getFullName() != null ? dto.getFullName() : p.getFullName());
            p.setPhoneNumber(dto.getAccountPhone());
            p.setAddress(ParseUtil.parseString(dto.getAddress()));
            p.setDob(dto.getDob());
            p.setGender(dto.getGender());
            p.setHeight(dto.getHeight());
            p.setWeight(dto.getWeight());
            p.setBloodgroup(ParseUtil.parseString(dto.getBloodgroup()));
            p.setPermanentMedicalHistory(ParseUtil.parseString(dto.getPermanentMedicalHistory()));
            p.setAllergyNotes(ParseUtil.parseString(dto.getAllergyNotes()));
            p.setSupervisorName(ParseUtil.parseString(dto.getSupervisorName()));
            p.setSupervisorPhone(ParseUtil.parseString(dto.getSupervisorPhone()));
            patientRepository.save(p);
        } else {
            Profile p = profileRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Profile record not found for user: " + userId));
            p.setFullName(dto.getFullName() != null ? dto.getFullName() : p.getFullName());
            p.setPhoneNumber(dto.getAccountPhone());
            p.setAddress(ParseUtil.parseString(dto.getAddress()));
            p.setDob(dto.getDob());
            p.setGender(dto.getGender());
            p.setSpecialty(ParseUtil.parseString(dto.getSpecialty()));
            // Ánh xạ phòng khám
            if (dto.getRoomName() != null && !dto.getRoomName().isBlank()) {
                Room room = roomRepository.findAll().stream()
                        .filter(r -> r.getRoomName().equalsIgnoreCase(dto.getRoomName()))
                        .findFirst()
                        .orElseGet(() -> roomRepository.save(new Room(dto.getRoomName())));
                p.setRoom(room);
            }
            profileRepository.save(p);
        }
        return dto;
    }

    @Override
    @Transactional
    public void toggleLock(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (User.STATUS_LOCKED.equals(user.getStatus())) {
            user.setStatus(User.STATUS_ACTIVE);
        } else {
            user.setStatus(User.STATUS_LOCKED);
        }
        userRepository.save(user);
    }
    @Override
    @Transactional(readOnly = true)
    public UserManagementDTO getUserManagementDTOById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        UserManagementDTO dto = new UserManagementDTO();
        dto.setUserId(user.getUserId());
        dto.setAccountPhone(user.getPhoneNumber());
        dto.setStatus(user.getStatus());
        if (user.getRole() != null) {
            dto.setRole(user.getRole().getRoleName().toLowerCase());
        }
        if (dto.getRole() != null && dto.getRole().contains("patient")) {
            patientRepository.findById(userId).ifPresent(p -> {
                dto.setFullName(p.getFullName());
                dto.setAccountPhone(p.getPhoneNumber());
                dto.setAddress(p.getAddress());
                dto.setDob(p.getDob());
                dto.setGender(p.getGender());
                dto.setHeight(p.getHeight());
                dto.setWeight(p.getWeight());
                dto.setBloodgroup(p.getBloodgroup());
                dto.setPermanentMedicalHistory(p.getPermanentMedicalHistory());
                dto.setAllergyNotes(p.getAllergyNotes());
                dto.setSupervisorName(p.getSupervisorName());
                dto.setSupervisorPhone(p.getSupervisorPhone());
            });
        } else {
            profileRepository.findById(userId).ifPresent(p -> {
                dto.setFullName(p.getFullName());
                dto.setAccountPhone(p.getPhoneNumber());
                dto.setAddress(p.getAddress());
                dto.setDob(p.getDob());
                dto.setGender(p.getGender());
                if (p.getRoom() != null) {
                    dto.setRoomName(p.getRoom().getRoomName());
                }
                dto.setSpecialty(p.getSpecialty());
            });
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPhoneTaken(String phone, String excludeUserId) {
        if (phone == null || phone.trim().isEmpty()) return false;
        Optional<User> existing = userRepository.findByPhoneNumber(phone.trim());
        if (existing.isEmpty()) return false;
        if (excludeUserId == null || excludeUserId.trim().isEmpty()) return true;
        return !existing.get().getUserId().equals(excludeUserId);
    }



}