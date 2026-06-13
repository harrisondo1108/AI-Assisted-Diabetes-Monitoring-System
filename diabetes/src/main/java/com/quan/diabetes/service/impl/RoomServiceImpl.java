package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.Room;
import com.quan.diabetes.repository.RoomRepository;
import com.quan.diabetes.service.RoomService;
import org.springframework.stereotype.Service;

import java.util.List;

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
}