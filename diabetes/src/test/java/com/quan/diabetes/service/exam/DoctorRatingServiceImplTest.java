package com.quan.diabetes.service.exam;

import com.quan.diabetes.dto.doctor.DoctorRatingView;
import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.DoctorRating;
import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.entity.Room;
import com.quan.diabetes.entity.Role;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.DoctorRatingRepository;
import com.quan.diabetes.repository.ProfileRepository;
import com.quan.diabetes.service.exam.impl.DoctorRatingServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorRatingServiceImplTest {

    @Mock
    private DoctorRatingRepository doctorRatingRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private DoctorRatingServiceImpl doctorRatingService;

    @Test
    @DisplayName("saveRating - Should call repository save")
    void saveRating_Success() {
        DoctorRating rating = new DoctorRating();
        doctorRatingService.saveRating(rating);
        verify(doctorRatingRepository, times(1)).save(rating);
    }

    @Test
    @DisplayName("getRatingByExamId - Should return optional rating")
    void getRatingByExamId_Success() {
        DoctorRating rating = new DoctorRating();
        when(doctorRatingRepository.findByClinicalExamination_ClinicalExamId("EX001"))
                .thenReturn(Optional.of(rating));

        Optional<DoctorRating> result = doctorRatingService.getRatingByExamId("EX001");
        assertTrue(result.isPresent());
        assertEquals(rating, result.get());
    }

    @Test
    @DisplayName("getRatingsByDoctor - List should return doctor ratings")
    void getRatingsByDoctor_List_Success() {
        DoctorRating rating = new DoctorRating();
        when(doctorRatingRepository.findByDoctor_UserId("DOC01")).thenReturn(List.of(rating));

        List<DoctorRating> list = doctorRatingService.getRatingsByDoctor("DOC01");
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("getRatingsByDoctor - Pageable should return paged doctor ratings")
    void getRatingsByDoctor_Pageable_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        DoctorRating rating = new DoctorRating();
        Page<DoctorRating> page = new PageImpl<>(List.of(rating));

        when(doctorRatingRepository.findByDoctor_UserId("DOC01", pageable)).thenReturn(page);

        Page<DoctorRating> result = doctorRatingService.getRatingsByDoctor("DOC01", pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("getRatingsByDoctorAndStar - Should return filtered paged ratings")
    void getRatingsByDoctorAndStar_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        DoctorRating rating = new DoctorRating();
        Page<DoctorRating> page = new PageImpl<>(List.of(rating));

        when(doctorRatingRepository.findByDoctor_UserIdAndRatingValue("DOC01", 5, pageable)).thenReturn(page);

        Page<DoctorRating> result = doctorRatingService.getRatingsByDoctorAndStar("DOC01", 5, pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("getAverageRatingForDoctor - When average is present should return value")
    void getAverageRatingForDoctor_WithValue() {
        when(doctorRatingRepository.findAverageRatingByDoctorId("DOC01")).thenReturn(4.5);

        Double avg = doctorRatingService.getAverageRatingForDoctor("DOC01");
        assertEquals(4.5, avg);
    }

    @Test
    @DisplayName("getAverageRatingForDoctor - When average is null should return 0.0")
    void getAverageRatingForDoctor_WithNull() {
        when(doctorRatingRepository.findAverageRatingByDoctorId("DOC01")).thenReturn(null);

        Double avg = doctorRatingService.getAverageRatingForDoctor("DOC01");
        assertEquals(0.0, avg);
    }

    @Test
    @DisplayName("getRatingsForExams - Null or empty list should return empty map")
    void getRatingsForExams_NullOrEmpty() {
        Map<String, DoctorRating> mapNull = doctorRatingService.getRatingsForExams(null);
        assertTrue(mapNull.isEmpty());

        Map<String, DoctorRating> mapEmpty = doctorRatingService.getRatingsForExams(Collections.emptyList());
        assertTrue(mapEmpty.isEmpty());
    }

    @Test
    @DisplayName("getRatingsForExams - Valid exam list should return map of examId to rating")
    void getRatingsForExams_ValidList() {
        DoctorRating r1 = new DoctorRating();
        when(doctorRatingRepository.findByClinicalExamination_ClinicalExamId("EX01")).thenReturn(Optional.of(r1));
        when(doctorRatingRepository.findByClinicalExamination_ClinicalExamId("EX02")).thenReturn(Optional.empty());

        Map<String, DoctorRating> result = doctorRatingService.getRatingsForExams(List.of("EX01", "EX02"));
        assertEquals(1, result.size());
        assertEquals(r1, result.get("EX01"));
    }

    @Test
    @DisplayName("getTopRatedDoctors - Should sort and return top rated doctor views")
    void getTopRatedDoctors_Success() {
        Role docRole = new Role();
        docRole.setRoleId("DOC");

        User u1 = new User();
        u1.setUserId("DOC1");
        u1.setRole(docRole);

        Profile p1 = new Profile();
        p1.setUserId("DOC1");
        p1.setUser(u1);
        p1.setFullName("Doctor One");
        p1.setSpecialty("Endocrinology");
        p1.setImageUrl("doc1.png");
        Room room1 = new Room();
        room1.setRoomName("Room A");
        p1.setRoom(room1);

        User u2 = new User();
        u2.setUserId("DOC2");
        u2.setRole(docRole);

        Profile p2 = new Profile();
        p2.setUserId("DOC2");
        p2.setUser(u2);
        p2.setFullName("Doctor Two");
        p2.setSpecialty("Cardiology");
        p2.setRoom(null); // test room fallback

        when(profileRepository.findAll()).thenReturn(List.of(p1, p2));
        when(doctorRatingRepository.findAverageRatingByDoctorId("DOC1")).thenReturn(4.8);
        when(doctorRatingRepository.countByDoctor_UserId("DOC1")).thenReturn(10L);

        when(doctorRatingRepository.findAverageRatingByDoctorId("DOC2")).thenReturn(null);
        when(doctorRatingRepository.countByDoctor_UserId("DOC2")).thenReturn(0L);

        List<DoctorRatingView> views = doctorRatingService.getTopRatedDoctors(2);
        assertEquals(2, views.size());
        assertEquals("DOC1", views.get(0).getDoctorId());
        assertEquals(4.8, views.get(0).getAverageRating());
        assertEquals("Room A", views.get(0).getRoomName());

        assertEquals("DOC2", views.get(1).getDoctorId());
        assertEquals(0.0, views.get(1).getAverageRating());
        assertEquals("Phòng nội tiết", views.get(1).getRoomName());
    }

    @Test
    @DisplayName("getAllRatings - Pageable should return all paged ratings")
    void getAllRatings_Pageable_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        DoctorRating rating = new DoctorRating();
        Page<DoctorRating> page = new PageImpl<>(List.of(rating));

        when(doctorRatingRepository.findAll(pageable)).thenReturn(page);

        Page<DoctorRating> result = doctorRatingService.getAllRatings(pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("getAllRatings - List should return all ratings")
    void getAllRatings_List_Success() {
        DoctorRating rating = new DoctorRating();
        when(doctorRatingRepository.findAll()).thenReturn(List.of(rating));

        List<DoctorRating> result = doctorRatingService.getAllRatings();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getRatingsByStar - Should return ratings by star value")
    void getRatingsByStar_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        DoctorRating rating = new DoctorRating();
        Page<DoctorRating> page = new PageImpl<>(List.of(rating));

        when(doctorRatingRepository.findByRatingValue(5, pageable)).thenReturn(page);

        Page<DoctorRating> result = doctorRatingService.getRatingsByStar(5, pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("getAverageRatingForAll - When value exists should return value, else 0.0")
    void getAverageRatingForAll_Success() {
        when(doctorRatingRepository.findAverageRatingForAll()).thenReturn(4.2);
        assertEquals(4.2, doctorRatingService.getAverageRatingForAll());

        when(doctorRatingRepository.findAverageRatingForAll()).thenReturn(null);
        assertEquals(0.0, doctorRatingService.getAverageRatingForAll());
    }
}
