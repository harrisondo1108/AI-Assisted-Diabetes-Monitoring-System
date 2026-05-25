package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.Room;
import com.quan.diabetes.repository.RoomRepository;
import com.quan.diabetes.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Override
    public Optional<Room> findById(Integer id) {
        return roomRepository.findById(id);
    }

    @Override
    public Room create(Room entity) {
        return roomRepository.save(entity);
    }

    @Override
    public Room update(Integer id, Room entity) {
        if (!roomRepository.existsById(id)) {
            throw new EntityNotFoundException("Room not found with id: " + id);
        }
        return roomRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        if (!roomRepository.existsById(id)) {
            throw new EntityNotFoundException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return roomRepository.existsById(id);
    }
}
