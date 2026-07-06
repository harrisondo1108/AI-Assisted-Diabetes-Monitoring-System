package com.quan.diabetes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing a Room.
 */
@Entity
@Table(name = "Room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoomID")
    private int roomId;

    @Column(name = "RoomName", nullable = false, unique = true, length = 100, columnDefinition = "NVARCHAR(100)")
    private String roomName;

    @Column(name = "Description", length = 255, columnDefinition = "NVARCHAR(255)")
    private String description;

    public Room() {}

    public Room(String roomName) {
        this.roomName = roomName;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Room{roomId=" + roomId + ", roomName='" + roomName + "', description='" + description + "'}";
    }
}
