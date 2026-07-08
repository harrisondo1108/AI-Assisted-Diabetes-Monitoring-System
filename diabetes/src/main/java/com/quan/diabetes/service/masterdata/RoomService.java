package com.quan.diabetes.service.masterdata;

import com.quan.diabetes.entity.Room;
import java.util.List;
import java.util.Optional;

public interface RoomService {

    public List<Room> findAll();

    public Optional<Room> findById(Integer id);

    public Room create(Room entity);

    public Room update(Integer id, Room entity);

    public void deleteById(Integer id);

    public boolean existsById(Integer id);

    public List<Room> searchByKeyword(String keyword);
}
