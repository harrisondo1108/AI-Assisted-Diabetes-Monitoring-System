package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.Room;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
}

