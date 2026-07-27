package com.quan.diabetes.service.masterdata;

import com.quan.diabetes.entity.Room;
import com.quan.diabetes.repository.RoomRepository;
import com.quan.diabetes.service.masterdata.impl.RoomServiceImpl;
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
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    @Test
    void testFindAll() {
        Room r1 = new Room();
        r1.setRoomId(1);
        Room r2 = new Room();
        r2.setRoomId(2);
        List<Room> mockList = Arrays.asList(r1, r2);

        when(roomRepository.findAll()).thenReturn(mockList);

        List<Room> result = roomService.findAll();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getRoomId());
        assertEquals(2, result.get(1).getRoomId());
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Found() {
        Room r = new Room();
        r.setRoomId(1);

        when(roomRepository.findById(1)).thenReturn(Optional.of(r));

        Optional<Room> result = roomService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getRoomId());
        verify(roomRepository, times(1)).findById(1);
    }

    @Test
    void testFindById_NotFound() {
        when(roomRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Room> result = roomService.findById(999);

        assertFalse(result.isPresent());
        verify(roomRepository, times(1)).findById(999);
    }

    @Test
    void testCreate() {
        Room input = new Room();
        input.setRoomName("Lab Room 101");

        when(roomRepository.save(input)).thenReturn(input);

        Room result = roomService.create(input);

        assertNotNull(result);
        assertEquals("Lab Room 101", result.getRoomName());
        verify(roomRepository, times(1)).save(input);
    }

    @Test
    void testUpdate_Success() {
        Integer id = 1;
        Room input = new Room();
        input.setRoomId(id);
        input.setRoomName("Updated Room");

        when(roomRepository.existsById(id)).thenReturn(true);
        when(roomRepository.save(input)).thenReturn(input);

        Room result = roomService.update(id, input);

        assertNotNull(result);
        assertEquals(id, result.getRoomId());
        assertEquals("Updated Room", result.getRoomName());
        verify(roomRepository, times(1)).existsById(id);
        verify(roomRepository, times(1)).save(input);
    }

    @Test
    void testUpdate_NotFound() {
        Integer id = 999;
        Room input = new Room();

        when(roomRepository.existsById(id)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> roomService.update(id, input));
        verify(roomRepository, times(1)).existsById(id);
        verify(roomRepository, never()).save(any());
    }

    @Test
    void testDeleteById_Success() {
        Integer id = 1;

        when(roomRepository.existsById(id)).thenReturn(true);

        roomService.deleteById(id);

        verify(roomRepository, times(1)).existsById(id);
        verify(roomRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteById_NotFound() {
        Integer id = 999;

        when(roomRepository.existsById(id)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> roomService.deleteById(id));
        verify(roomRepository, times(1)).existsById(id);
        verify(roomRepository, never()).deleteById(any());
    }

    @Test
    void testExistsById() {
        when(roomRepository.existsById(1)).thenReturn(true);

        assertTrue(roomService.existsById(1));
        verify(roomRepository, times(1)).existsById(1);
    }

    @Test
    void testSearchByKeyword_NullOrEmpty() {
        Room r1 = new Room();
        r1.setRoomName("Room A");
        List<Room> allRooms = List.of(r1);

        when(roomRepository.findAll()).thenReturn(allRooms);

        List<Room> nullResult = roomService.searchByKeyword(null);
        assertEquals(1, nullResult.size());

        List<Room> emptyResult = roomService.searchByKeyword("   ");
        assertEquals(1, emptyResult.size());
    }

    @Test
    void testSearchByKeyword_WithMatches() {
        Room r1 = new Room();
        r1.setRoomName("Lab Test Room");
        r1.setDescription("Blood testing facility");

        Room r2 = new Room();
        r2.setRoomName("Consulting Room");
        r2.setDescription("General checkup");

        when(roomRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

        // Match by roomName
        List<Room> result1 = roomService.searchByKeyword("Lab");
        assertEquals(1, result1.size());
        assertEquals("Lab Test Room", result1.get(0).getRoomName());

        // Match by description
        List<Room> result2 = roomService.searchByKeyword("checkup");
        assertEquals(1, result2.size());
        assertEquals("Consulting Room", result2.get(0).getRoomName());

        // No match
        List<Room> result3 = roomService.searchByKeyword("XRay");
        assertEquals(0, result3.size());
    }
}
