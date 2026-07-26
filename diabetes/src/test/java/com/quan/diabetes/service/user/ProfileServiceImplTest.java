package com.quan.diabetes.service.user;

import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.entity.Role;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ProfileServiceImpl.class})
class ProfileServiceImplTest {

    @MockitoBean
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileServiceImpl profileService;

    private Profile docProfile;
    private Profile patProfile;

    @BeforeEach
    void setUp() {
        Role docRole = new Role();
        docRole.setRoleId("DOC");
        docRole.setRoleName("Doctor");

        User docUser = new User();
        docUser.setUserId("DOC001");
        docUser.setRole(docRole);

        docProfile = new Profile();
        docProfile.setUserId("DOC001");
        docProfile.setUser(docUser);
        docProfile.setFullName("Dr. Smith");

        Role patRole = new Role();
        patRole.setRoleId("PAT");
        patRole.setRoleName("Patient");

        User patUser = new User();
        patUser.setUserId("PAT001");
        patUser.setRole(patRole);

        patProfile = new Profile();
        patProfile.setUserId("PAT001");
        patProfile.setUser(patUser);
        patProfile.setFullName("Patient John");
    }

    @Test
    void testFindAll() {
        when(profileRepository.findAll()).thenReturn(Collections.singletonList(docProfile));

        List<Profile> result = profileService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DOC001", result.get(0).getUserId());
        verify(profileRepository, times(1)).findAll();
    }

    @Test
    void testFindTotalDoctor_IncludesNullAndBranchEdgeCases() {
        Profile nullUserProf = new Profile();
        nullUserProf.setUserId("NULL001");

        Profile nullRoleProf = new Profile();
        nullRoleProf.setUserId("NULL002");
        nullRoleProf.setUser(new User());

        // Test list containing: valid doc, valid pat, null profile, null user, null role
        when(profileRepository.findAll()).thenReturn(Arrays.asList(docProfile, patProfile, null, nullUserProf, nullRoleProf));

        List<Profile> doctors = profileService.findTotalDoctor();

        assertNotNull(doctors);
        assertEquals(1, doctors.size());
        assertEquals("DOC001", doctors.get(0).getUserId());
    }

    @Test
    void testFindById_Found() {
        when(profileRepository.findById("DOC001")).thenReturn(Optional.of(docProfile));

        Optional<Profile> result = profileService.findById("DOC001");

        assertTrue(result.isPresent());
        assertEquals("DOC001", result.get().getUserId());
        verify(profileRepository, times(1)).findById("DOC001");
    }

    @Test
    void testFindById_NotFound() {
        when(profileRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        Optional<Profile> result = profileService.findById("UNKNOWN");

        assertFalse(result.isPresent());

        verify(profileRepository, times(1)).findById("UNKNOWN");
    }

    @Test
    void testCreate() {
        when(profileRepository.save(any(Profile.class))).thenReturn(docProfile);

        Profile created = profileService.create(docProfile);

        assertNotNull(created);
        assertEquals("DOC001", created.getUserId());
        verify(profileRepository, times(1)).save(docProfile);
    }
}
