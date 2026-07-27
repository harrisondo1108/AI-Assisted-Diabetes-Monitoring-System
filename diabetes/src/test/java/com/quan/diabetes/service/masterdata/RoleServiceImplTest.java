package com.quan.diabetes.service.masterdata;

import com.quan.diabetes.entity.Role;
import com.quan.diabetes.repository.RoleRepository;
import com.quan.diabetes.service.masterdata.impl.RoleServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void testFindAll() {
        Role r1 = new Role();
        r1.setRoleId("ROLE_ADMIN");
        Role r2 = new Role();
        r2.setRoleId("ROLE_PATIENT");
        List<Role> mockList = Arrays.asList(r1, r2);

        when(roleRepository.findAll()).thenReturn(mockList);

        List<Role> result = roleService.findAll();

        assertEquals(2, result.size());
        assertEquals("ROLE_ADMIN", result.get(0).getRoleId());
        assertEquals("ROLE_PATIENT", result.get(1).getRoleId());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Found() {
        Role r = new Role();
        r.setRoleId("ROLE_ADMIN");

        when(roleRepository.findById("ROLE_ADMIN")).thenReturn(Optional.of(r));

        Optional<Role> result = roleService.findById("ROLE_ADMIN");

        assertTrue(result.isPresent());
        assertEquals("ROLE_ADMIN", result.get().getRoleId());
        verify(roleRepository, times(1)).findById("ROLE_ADMIN");
    }

    @Test
    void testFindById_NotFound() {
        when(roleRepository.findById("ROLE_UNKNOWN")).thenReturn(Optional.empty());

        Optional<Role> result = roleService.findById("ROLE_UNKNOWN");

        assertFalse(result.isPresent());
        verify(roleRepository, times(1)).findById("ROLE_UNKNOWN");
    }

    @Test
    void testCreate() {
        Role input = new Role();
        input.setRoleId("ROLE_DOCTOR");

        when(roleRepository.save(input)).thenReturn(input);

        Role result = roleService.create(input);

        assertNotNull(result);
        assertEquals("ROLE_DOCTOR", result.getRoleId());
        verify(roleRepository, times(1)).save(input);
    }

    @Test
    void testUpdate_Success() {
        String id = "ROLE_ADMIN";
        Role input = new Role();
        input.setRoleId(id);

        when(roleRepository.existsById(id)).thenReturn(true);
        when(roleRepository.save(input)).thenReturn(input);

        Role result = roleService.update(id, input);

        assertNotNull(result);
        assertEquals(id, result.getRoleId());
        verify(roleRepository, times(1)).existsById(id);
        verify(roleRepository, times(1)).save(input);
    }

    @Test
    void testUpdate_NotFound() {
        String id = "ROLE_UNKNOWN";
        Role input = new Role();

        when(roleRepository.existsById(id)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> roleService.update(id, input));
        verify(roleRepository, times(1)).existsById(id);
        verify(roleRepository, never()).save(any());
    }

    @Test
    void testDeleteById_Success() {
        String id = "ROLE_ADMIN";

        when(roleRepository.existsById(id)).thenReturn(true);

        roleService.deleteById(id);

        verify(roleRepository, times(1)).existsById(id);
        verify(roleRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteById_NotFound() {
        String id = "ROLE_UNKNOWN";

        when(roleRepository.existsById(id)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> roleService.deleteById(id));
        verify(roleRepository, times(1)).existsById(id);
        verify(roleRepository, never()).deleteById(anyString());
    }

    @Test
    void testExistsById() {
        when(roleRepository.existsById("ROLE_ADMIN")).thenReturn(true);

        assertTrue(roleService.existsById("ROLE_ADMIN"));
        verify(roleRepository, times(1)).existsById("ROLE_ADMIN");
    }
}
