package com.quan.diabetes.service.user;

import com.quan.diabetes.entity.Role;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserServiceImpl.class})
class UserServiceImplTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserServiceImpl userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId("PAT001");
        sampleUser.setPhoneNumber("0987654321");
        sampleUser.setPasswordHash("rawPassword");

        Role role = new Role();
        role.setRoleId("PAT");
        role.setRoleName("Patient");
        sampleUser.setRole(role);
    }

    @Test
    void testFindById_Found() {
        when(userRepository.findById("PAT001")).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.findById("PAT001");

        assertTrue(result.isPresent());
        assertEquals("PAT001", result.get().getUserId());
        verify(userRepository, times(1)).findById("PAT001");
    }

    @Test
    void testFindById_NotFound() {
        when(userRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        Optional<User> result = userService.findById("UNKNOWN");

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById("UNKNOWN");
    }

    @Test
    void testFindByUsernameAndPassword_Success() {
        when(userRepository.findByPhoneNumber("0987654321")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("rawPassword", "rawPassword")).thenReturn(true);

        Optional<User> result = userService.findByUsernameAndPassword("0987654321", "rawPassword");

        assertTrue(result.isPresent());
        assertEquals("PAT001", result.get().getUserId());
    }

    @Test
    void testFindByUsernameAndPassword_WrongPassword() {
        when(userRepository.findByPhoneNumber("0987654321")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPassword", "rawPassword")).thenReturn(false);

        Optional<User> result = userService.findByUsernameAndPassword("0987654321", "wrongPassword");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByUsernameAndPassword_UserNotFound() {
        when(userRepository.findByPhoneNumber("0000000000")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsernameAndPassword("0000000000", "rawPassword");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByPhoneNumber() {
        when(userRepository.findByPhoneNumber("0987654321")).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.findByPhoneNumber("0987654321");

        assertTrue(result.isPresent());
        assertEquals("PAT001", result.get().getUserId());
    }

    @Test
    void testCreate() {
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.create(sampleUser);

        assertNotNull(created);
        assertEquals("PAT001", created.getUserId());
        assertEquals("encodedPassword", created.getPasswordHash());
        verify(passwordEncoder, times(1)).encode("rawPassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdate_Success_RawPassword() {
        when(userRepository.existsById("PAT001")).thenReturn(true);
        when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.update("PAT001", sampleUser);

        assertNotNull(updated);
        assertEquals("$2a$encodedPassword", updated.getPasswordHash());
        verify(passwordEncoder, times(1)).encode("rawPassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdate_Success_AlreadyEncodedPassword_2a() {
        sampleUser.setPasswordHash("$2a$10$alreadyEncodedHash");
        when(userRepository.existsById("PAT001")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.update("PAT001", sampleUser);

        assertNotNull(updated);
        assertEquals("$2a$10$alreadyEncodedHash", updated.getPasswordHash());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdate_Success_AlreadyEncodedPassword_2b() {
        sampleUser.setPasswordHash("$2b$10$alreadyEncodedHash2b");
        when(userRepository.existsById("PAT001")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.update("PAT001", sampleUser);

        assertNotNull(updated);
        assertEquals("$2b$10$alreadyEncodedHash2b", updated.getPasswordHash());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testUpdate_Success_AlreadyEncodedPassword_2y() {
        sampleUser.setPasswordHash("$2y$10$alreadyEncodedHash2y");
        when(userRepository.existsById("PAT001")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.update("PAT001", sampleUser);

        assertNotNull(updated);
        assertEquals("$2y$10$alreadyEncodedHash2y", updated.getPasswordHash());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testUpdate_Success_NullPassword() {
        sampleUser.setPasswordHash(null);
        when(userRepository.existsById("PAT001")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.update("PAT001", sampleUser);

        assertNotNull(updated);
        assertNull(updated.getPasswordHash());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testUpdate_UserNotFound() {
        when(userRepository.existsById("UNKNOWN")).thenReturn(false);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            userService.update("UNKNOWN", sampleUser);
        });

        assertEquals("User not found with id: UNKNOWN", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetNewID_PAT_NoCollision() {
        when(userRepository.existsById(anyString())).thenReturn(false);

        String id = userService.getNewID("PAT");

        assertNotNull(id);
        assertTrue(id.startsWith("P"));
        assertEquals(7, id.length());
    }

    @Test
    void testGetNewID_PAT_WithCollision() {
        when(userRepository.existsById(anyString())).thenReturn(true).thenReturn(false);

        String id = userService.getNewID("PAT");

        assertNotNull(id);
        assertTrue(id.startsWith("P"));
        assertEquals(7, id.length());
        verify(userRepository, times(2)).existsById(anyString());
    }

    @Test
    void testGetNewID_DOC_NoCollision() {
        when(userRepository.existsById(anyString())).thenReturn(false);

        String id = userService.getNewID("DOC");

        assertNotNull(id);
        assertTrue(id.startsWith("D"));
        assertEquals(7, id.length());
    }

    @Test
    void testGetNewID_DOC_WithCollision() {
        when(userRepository.existsById(anyString())).thenReturn(true).thenReturn(false);

        String id = userService.getNewID("DOC");

        assertNotNull(id);
        assertTrue(id.startsWith("D"));
        assertEquals(7, id.length());
        verify(userRepository, times(2)).existsById(anyString());
    }

    @Test
    void testGetNewID_UnknownRole() {
        String id = userService.getNewID("UNKNOWN");
        assertNull(id);
    }

    @Test
    void testGetNewID_NullRole_ReturnsNull() {
        String id = userService.getNewID(null);
        assertNull(id);
    }

    /**
     * update() with password having an unrecognized BCrypt prefix like "$2x$":
     * pwd != null is true, but startsWith("$2a$"), startsWith("$2b$"), startsWith("$2y$")
     * ALL evaluate to false. Then goes to line 71 else if (pwd != null) -> encodes it.
     */
    @Test
    void testUpdate_Success_UnrecognizedBcryptPrefix() {
        sampleUser.setPasswordHash("$2x$10$unrecognizedHash");
        when(userRepository.existsById("PAT001")).thenReturn(true);
        when(passwordEncoder.encode("$2x$10$unrecognizedHash")).thenReturn("encodedUnrecognized");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.update("PAT001", sampleUser);

        assertNotNull(updated);
        assertEquals("encodedUnrecognized", updated.getPasswordHash());
        verify(passwordEncoder, times(1)).encode("$2x$10$unrecognizedHash");
    }
}

