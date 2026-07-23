package com.quan.diabetes.service.medication;

import com.quan.diabetes.entity.Medication;
import com.quan.diabetes.repository.MedicationRepository;
import com.quan.diabetes.service.medication.impl.MedicationServiceImpl;
import com.quan.diabetes.service.systemlog.SystemLogService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationServiceImplTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private SystemLogService systemLogService;

    @InjectMocks
    private MedicationServiceImpl medicationService;

    private Medication validMedication;

    @BeforeEach
    void setUp() {
        validMedication = new Medication();
        validMedication.setMedicationId("MED-01");
        validMedication.setMedicationName("Paracetamol");
        validMedication.setForm("Tablet");
        validMedication.setAdministrationRoute("Oral");
        validMedication.setConcentration("500mg");
        validMedication.setUsageInstruction("Uống sau ăn");
        validMedication.setStatus("Active");
    }

    @Test
    void testFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medication> expectedPage = new PageImpl<>(List.of(validMedication));
        when(medicationRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Medication> result = medicationService.findAll(pageable);
        assertEquals(expectedPage, result);
        verify(medicationRepository).findAll(pageable);
    }

    @Test
    void testFindAllActive() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medication> expectedPage = new PageImpl<>(List.of(validMedication));
        when(medicationRepository.findAllActive(pageable)).thenReturn(expectedPage);

        Page<Medication> result = medicationService.findAllActive(pageable);
        assertEquals(expectedPage, result);
        verify(medicationRepository).findAllActive(pageable);
    }

    @Test
    void testFindAllClocked() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medication> expectedPage = new PageImpl<>(List.of(validMedication));
        when(medicationRepository.findAllClocked(pageable)).thenReturn(expectedPage);

        Page<Medication> result = medicationService.findAllClocked(pageable);
        assertEquals(expectedPage, result);
        verify(medicationRepository).findAllClocked(pageable);
    }

    @Test
    void testFindByForm() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medication> expectedPage = new PageImpl<>(List.of(validMedication));
        when(medicationRepository.findByForm("Tablet", pageable)).thenReturn(expectedPage);

        Page<Medication> result = medicationService.findByForm("Tablet", pageable);
        assertEquals(expectedPage, result);
        verify(medicationRepository).findByForm("Tablet", pageable);
    }

    @Test
    void testFindByAdministrationRoute() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medication> expectedPage = new PageImpl<>(List.of(validMedication));
        when(medicationRepository.findByAdministrationRoute("Oral", pageable)).thenReturn(expectedPage);

        Page<Medication> result = medicationService.findByAdministrationRoute("Oral", pageable);
        assertEquals(expectedPage, result);
        verify(medicationRepository).findByAdministrationRoute("Oral", pageable);
    }

    @Test
    void testSearchByKeyword_NullOrEmptyKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medication> expectedPage = new PageImpl<>(List.of(validMedication));
        when(medicationRepository.findAll(pageable)).thenReturn(expectedPage);

        // Branch 1: keyword is null
        Page<Medication> resultNull = medicationService.searchByKeyword(null, pageable);
        assertEquals(expectedPage, resultNull);

        // Branch 2: keyword is empty (trim().isEmpty() == true)
        Page<Medication> resultEmpty = medicationService.searchByKeyword("   ", pageable);
        assertEquals(expectedPage, resultEmpty);

        verify(medicationRepository, times(2)).findAll(pageable);
    }

    @Test
    void testSearchByKeyword_NonEmptyKeyword_StartLessThanTotal() {
        Pageable pageable = PageRequest.of(0, 10); // offset = 0, pageSize = 10

        Medication med1 = new Medication();
        med1.setMedicationName("Paracetamol"); // matches keyword "para"
        med1.setAdministrationRoute("Oral");

        Medication med2 = new Medication();
        med2.setMedicationName("Aspirin"); // doesn't match name
        med2.setAdministrationRoute("Paracenter Route"); // matches route "para"

        Medication med3 = new Medication();
        med3.setMedicationName("Insulin"); // neither matches
        med3.setAdministrationRoute("Injection");

        when(medicationRepository.findAll()).thenReturn(List.of(med1, med2, med3));

        // Branch: start < total (0 < 2) and filter branches (name match, route match, neither match)
        Page<Medication> result = medicationService.searchByKeyword("para", pageable);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().contains(med1));
        assertTrue(result.getContent().contains(med2));
    }

    @Test
    void testSearchByKeyword_NonEmptyKeyword_StartGreaterOrEqualTotal() {
        Pageable pageable = PageRequest.of(5, 10); // offset = 50, pageSize = 10

        Medication med1 = new Medication();
        med1.setMedicationName("Paracetamol");
        med1.setAdministrationRoute("Oral");

        when(medicationRepository.findAll()).thenReturn(List.of(med1));

        // Branch: start >= total (50 >= 1)
        Page<Medication> result = medicationService.searchByKeyword("para", pageable);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testFilterMedications() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medication> expectedPage = new PageImpl<>(List.of(validMedication));

        // Branch 1 for each arg: null
        when(medicationRepository.filterMedications("", "", "", "", pageable)).thenReturn(expectedPage);
        Page<Medication> res1 = medicationService.filterMedications(null, null, null, null, pageable);
        assertEquals(expectedPage, res1);

        // Branch 2 for each arg: "all" (case insensitive, trimmed)
        Page<Medication> res2 = medicationService.filterMedications("all", " ALL ", "All", "all", pageable);
        assertEquals(expectedPage, res2);

        // Branch 3 for each arg: not null and not "all"
        when(medicationRepository.filterMedications("kw", "Active", "Tablet", "Oral", pageable)).thenReturn(expectedPage);
        Page<Medication> res3 = medicationService.filterMedications("  kw ", " Active ", " Tablet ", " Oral ", pageable);
        assertEquals(expectedPage, res3);
    }

    @Test
    void testFindAllList() {
        when(medicationRepository.findAll()).thenReturn(List.of(validMedication));
        List<Medication> result = medicationService.findAllList();
        assertEquals(List.of(validMedication), result);
        verify(medicationRepository).findAll();
    }

    @Test
    void testFindAllActiveList() {
        when(medicationRepository.findAllActiveList()).thenReturn(List.of(validMedication));
        List<Medication> result = medicationService.findAllActiveList();
        assertEquals(List.of(validMedication), result);
        verify(medicationRepository).findAllActiveList();
    }

    @Test
    void testFindAllClockedList() {
        when(medicationRepository.findAllClockedList()).thenReturn(List.of(validMedication));
        List<Medication> result = medicationService.findAllClockedList();
        assertEquals(List.of(validMedication), result);
        verify(medicationRepository).findAllClockedList();
    }

    @Test
    void testFindById() {
        when(medicationRepository.findById("MED-01")).thenReturn(Optional.of(validMedication));
        Optional<Medication> result = medicationService.findById("MED-01");
        assertTrue(result.isPresent());
        assertEquals(validMedication, result.get());
        verify(medicationRepository).findById("MED-01");
    }

    @Test
    void testValidateAndTrimMedication_NullEntity() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> medicationService.create(null));
        assertEquals("Dữ liệu thuốc không hợp lệ!", ex.getMessage());
    }

    @Test
    void testValidateAndTrimMedication_NullOrEmptyName() {
        Medication medNullName = new Medication();
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> medicationService.create(medNullName));
        assertEquals("Tên thuốc không được để trống!", ex1.getMessage());

        Medication medEmptyName = new Medication();
        medEmptyName.setMedicationName("   ");
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> medicationService.create(medEmptyName));
        assertEquals("Tên thuốc không được để trống!", ex2.getMessage());
    }

    @Test
    void testValidateAndTrimMedication_SpecialCharName() {
        Medication med = new Medication();
        med.setMedicationName("Paracetamol@123");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> medicationService.create(med));
        assertEquals("Tên thuốc không được chứa ký tự đặc biệt!", ex.getMessage());
    }

    @Test
    void testValidateAndTrimMedication_NullOrEmptyForm() {
        Medication medNullForm = new Medication();
        medNullForm.setMedicationName("Paracetamol");
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> medicationService.create(medNullForm));
        assertEquals("Dạng bào chế không được để trống!", ex1.getMessage());

        Medication medEmptyForm = new Medication();
        medEmptyForm.setMedicationName("Paracetamol");
        medEmptyForm.setForm("   ");
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> medicationService.create(medEmptyForm));
        assertEquals("Dạng bào chế không được để trống!", ex2.getMessage());
    }

    @Test
    void testValidateAndTrimMedication_NullOrEmptyRoute() {
        Medication medNullRoute = new Medication();
        medNullRoute.setMedicationName("Paracetamol");
        medNullRoute.setForm("Tablet");
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> medicationService.create(medNullRoute));
        assertEquals("Đường dùng không được để trống!", ex1.getMessage());

        Medication medEmptyRoute = new Medication();
        medEmptyRoute.setMedicationName("Paracetamol");
        medEmptyRoute.setForm("Tablet");
        medEmptyRoute.setAdministrationRoute("   ");
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> medicationService.create(medEmptyRoute));
        assertEquals("Đường dùng không được để trống!", ex2.getMessage());
    }

    @Test
    void testCreate_MedicationNameAlreadyExists() {
        Medication med = new Medication();
        med.setMedicationName("Paracetamol");
        med.setForm("Tablet");
        med.setAdministrationRoute("Oral");

        when(medicationRepository.existsByMedicationNameIgnoreCase("Paracetamol")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> medicationService.create(med));
        assertEquals("Tên thuốc 'Paracetamol' đã tồn tại trong hệ thống!", ex.getMessage());
    }

    @Test
    void testCreate_SuccessAndGenerateMedicationId_WithBranches() {
        // Prepare list with various ID patterns to cover all branches of generateMedicationId()
        Medication m1 = new Medication();
        m1.setMedicationId(null); // id == null

        Medication m2 = new Medication();
        m2.setMedicationId("OTHER-01"); // !id.startsWith("MED-")

        Medication m3 = new Medication();
        m3.setMedicationId("MED-ABC"); // NumberFormatException

        Medication m4 = new Medication();
        m4.setMedicationId("MED-05"); // number = 5 > maxNumber (0) -> maxNumber = 5

        Medication m5 = new Medication();
        m5.setMedicationId("MED-02"); // number = 2 <= maxNumber (5)

        when(medicationRepository.existsByMedicationNameIgnoreCase("Paracetamol 500")).thenReturn(false);
        when(medicationRepository.findAll()).thenReturn(List.of(m1, m2, m3, m4, m5));
        when(medicationRepository.save(any(Medication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Medication entity = new Medication();
        entity.setMedicationName("  Paracetamol 500  ");
        entity.setForm("  Tablet  ");
        entity.setAdministrationRoute("  Oral  ");
        entity.setConcentration("  500mg  ");
        entity.setUsageInstruction("  Take after meal  ");

        Medication created = medicationService.create(entity);

        assertEquals("MED-06", created.getMedicationId());
        assertEquals("Active", created.getStatus());
        assertEquals("Paracetamol 500", created.getMedicationName());
        assertEquals("Tablet", created.getForm());
        assertEquals("Oral", created.getAdministrationRoute());
        assertEquals("500mg", created.getConcentration());
        assertEquals("Take after meal", created.getUsageInstruction());

        verify(systemLogService).saveLogWithObject(
                eq(null), eq("CREATE"), eq("Medicine"), eq("MED-06"),
                eq("Thêm thuốc mới"), eq(null), eq(created), eq("SUCCESS")
        );
    }

    @Test
    void testCreate_Success_EmptyMedicationsList_NullOptionalFields() {
        when(medicationRepository.existsByMedicationNameIgnoreCase("Insulin")).thenReturn(false);
        when(medicationRepository.findAll()).thenReturn(Collections.emptyList());
        when(medicationRepository.save(any(Medication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Medication entity = new Medication();
        entity.setMedicationName("Insulin");
        entity.setForm("Injection");
        entity.setAdministrationRoute("SC");
        entity.setConcentration(null);
        entity.setUsageInstruction(null);

        Medication created = medicationService.create(entity);

        assertEquals("MED-01", created.getMedicationId());
        assertNull(created.getConcentration());
        assertNull(created.getUsageInstruction());
    }

    @Test
    void testUpdate_EntityNotFound() {
        when(medicationRepository.existsById("MED-99")).thenReturn(false);
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> medicationService.update("MED-99", validMedication));
        assertEquals("Không tìm thấy thuốc với mã: MED-99", ex.getMessage());
    }

    @Test
    void testUpdate_ExistingNull_ThrowsNPEOnLine184() {
        when(medicationRepository.existsById("MED-01")).thenReturn(true);
        when(medicationRepository.findById("MED-01")).thenReturn(Optional.empty());

        Medication entity = new Medication();
        entity.setMedicationName("Paracetamol");
        entity.setForm("Tablet");
        entity.setAdministrationRoute("Oral");

        // Covers existing == null (if (existing != null) -> false)
        assertThrows(NullPointerException.class, () -> medicationService.update("MED-01", entity));
    }

    @Test
    void testUpdate_NameNotChanged_StatusIsNull() {
        when(medicationRepository.existsById("MED-01")).thenReturn(true);
        when(medicationRepository.findById("MED-01")).thenReturn(Optional.of(validMedication));
        when(medicationRepository.save(any(Medication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Medication entity = new Medication();
        entity.setMedicationName("Paracetamol"); // Same name as existing
        entity.setForm("Tablet");
        entity.setAdministrationRoute("Oral");
        entity.setStatus(null); // Should pick existing.getStatus() ("Active")

        Medication updated = medicationService.update("MED-01", entity);

        assertEquals("MED-01", updated.getMedicationId());
        assertEquals("Active", updated.getStatus());

        verify(medicationRepository, never()).existsByMedicationNameIgnoreCase(anyString());
        verify(systemLogService).saveLogWithObject(
                eq(null), eq("UPDATE"), eq("Medicine"), eq("MED-01"),
                eq("Cập nhật thông tin thuốc"), any(Medication.class), eq(updated), eq("SUCCESS")
        );
    }

    @Test
    void testUpdate_NameChangedAndExists() {
        when(medicationRepository.existsById("MED-01")).thenReturn(true);
        when(medicationRepository.findById("MED-01")).thenReturn(Optional.of(validMedication));
        when(medicationRepository.existsByMedicationNameIgnoreCase("New Med")).thenReturn(true);

        Medication entity = new Medication();
        entity.setMedicationName("New Med"); // Different name
        entity.setForm("Tablet");
        entity.setAdministrationRoute("Oral");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> medicationService.update("MED-01", entity));
        assertEquals("Tên thuốc 'New Med' đã tồn tại trong hệ thống!", ex.getMessage());
    }

    @Test
    void testUpdate_NameChangedAndNotExists_StatusNotNull() {
        when(medicationRepository.existsById("MED-01")).thenReturn(true);
        when(medicationRepository.findById("MED-01")).thenReturn(Optional.of(validMedication));
        when(medicationRepository.existsByMedicationNameIgnoreCase("New Med")).thenReturn(false);
        when(medicationRepository.save(any(Medication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Medication entity = new Medication();
        entity.setMedicationName("New Med");
        entity.setForm("Tablet");
        entity.setAdministrationRoute("Oral");
        entity.setStatus("Clocked"); // Non-null status

        Medication updated = medicationService.update("MED-01", entity);

        assertEquals("MED-01", updated.getMedicationId());
        assertEquals("Clocked", updated.getStatus());
        assertEquals("New Med", updated.getMedicationName());
    }

    @Test
    void testUpdateStatus_EntityNotFound() {
        when(medicationRepository.existsById("MED-99")).thenReturn(false);
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> medicationService.updateStatus("MED-99", "Clocked"));
        assertEquals("Medication not found with id: MED-99", ex.getMessage());
    }

    @Test
    void testUpdateStatus_Success() {
        when(medicationRepository.existsById("MED-01")).thenReturn(true);
        medicationService.updateStatus("MED-01", "Clocked");
        verify(medicationRepository).updateStatus("MED-01", "Clocked");
    }

    @Test
    void testSoftDelete() {
        when(medicationRepository.existsById("MED-01")).thenReturn(true);
        medicationService.softDelete("MED-01");
        verify(medicationRepository).updateStatus("MED-01", "Clocked");
    }

    @Test
    void testRestore() {
        when(medicationRepository.existsById("MED-01")).thenReturn(true);
        medicationService.restore("MED-01");
        verify(medicationRepository).updateStatus("MED-01", "Active");
    }

    @Test
    void testDeleteById_EntityNotFound() {
        when(medicationRepository.findById("MED-99")).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> medicationService.deleteById("MED-99"));
        assertEquals("Medication not found with id: MED-99", ex.getMessage());
    }

    @Test
    void testDeleteById_Success() {
        when(medicationRepository.findById("MED-01")).thenReturn(Optional.of(validMedication));
        medicationService.deleteById("MED-01");
        verify(medicationRepository).deleteById("MED-01");
        verify(systemLogService).saveLogWithObject(
                eq(null), eq("DELETE"), eq("Medicine"), eq("MED-01"),
                eq("Xóa thuốc"), eq(validMedication), eq(null), eq("SUCCESS")
        );
    }

    @Test
    void testExistsById() {
        when(medicationRepository.existsById("MED-01")).thenReturn(true);
        assertTrue(medicationService.existsById("MED-01"));

        when(medicationRepository.existsById("MED-99")).thenReturn(false);
        assertFalse(medicationService.existsById("MED-99"));
    }

    @Test
    void testExistsByMedicationName() {
        when(medicationRepository.existsByMedicationNameIgnoreCase("Paracetamol")).thenReturn(true);
        assertTrue(medicationService.existsByMedicationName("Paracetamol"));

        when(medicationRepository.existsByMedicationNameIgnoreCase("Unknown")).thenReturn(false);
        assertFalse(medicationService.existsByMedicationName("Unknown"));
    }

    @Test
    void testSearchByKeywordList_NullOrEmptyKeyword() {
        when(medicationRepository.findAll()).thenReturn(List.of(validMedication));

        List<Medication> resNull = medicationService.searchByKeywordList(null);
        assertEquals(List.of(validMedication), resNull);

        List<Medication> resEmpty = medicationService.searchByKeywordList("   ");
        assertEquals(List.of(validMedication), resEmpty);

        verify(medicationRepository, times(2)).findAll();
    }

    @Test
    void testSearchByKeywordList_NonEmptyKeyword_CoverAllBranches() {
        Medication med1 = new Medication();
        med1.setMedicationName("Keyword Med"); // name matches

        Medication med2 = new Medication();
        med2.setMedicationName("Other");
        med2.setConcentration("100mg keyword"); // concentration matches

        Medication med3 = new Medication();
        med3.setMedicationName("Other");
        med3.setConcentration("50mg");
        med3.setAdministrationRoute("Oral keyword"); // route matches

        Medication med4 = new Medication();
        med4.setMedicationName("Other");
        med4.setConcentration("50mg");
        med4.setAdministrationRoute("Oral"); // none match

        when(medicationRepository.findAll()).thenReturn(List.of(med1, med2, med3, med4));

        List<Medication> result = medicationService.searchByKeywordList("keyword");
        assertEquals(3, result.size());
        assertTrue(result.contains(med1));
        assertTrue(result.contains(med2));
        assertTrue(result.contains(med3));
    }

    @Test
    void testFindAllDistinctRoutes() {
        List<String> routes = List.of("Oral", "Injection");
        when(medicationRepository.findAllDistinctRoutes()).thenReturn(routes);
        assertEquals(routes, medicationService.findAllDistinctRoutes());
    }

    @Test
    void testGetSummary() {
        when(medicationRepository.countTotalMedications()).thenReturn(10L);
        when(medicationRepository.countActiveMedications()).thenReturn(8L);
        when(medicationRepository.countClockedMedications()).thenReturn(2L);
        when(medicationRepository.findAllDistinctRoutes()).thenReturn(List.of("Oral", "Injection"));

        Medication m1 = new Medication();
        m1.setForm(null); // form == null

        Medication m2 = new Medication();
        m2.setForm("Tablet"); // oral

        Medication m3 = new Medication();
        m3.setForm("Capsule"); // oral

        Medication m4 = new Medication();
        m4.setForm("Injection"); // injectable

        Medication m5 = new Medication();
        m5.setForm("Syrup"); // neither oral nor injectable

        when(medicationRepository.findAll()).thenReturn(List.of(m1, m2, m3, m4, m5));

        Map<String, Object> summary = medicationService.getSummary();

        assertEquals(10L, summary.get("totalMedications"));
        assertEquals(8L, summary.get("activeMedications"));
        assertEquals(2L, summary.get("clockedMedications"));
        assertEquals(2L, summary.get("oralFormulations"));
        assertEquals(1L, summary.get("injectableFormulations"));
    }
}
