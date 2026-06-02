package com.quan.diabetes.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Lab_Test_Catalog")
public class LabTestCatalog {

    @Id
    @Column(name = "LabTestID", length = 50)
    private String labTestId;

    @Column(name = "TestName", length = 100, unique = true, columnDefinition = "NVARCHAR(100)")
    private String testName;

    @Column(name = "Unit", length = 20, columnDefinition = "NVARCHAR(20)")
    private String unit;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @ManyToOne
    @JoinColumn(name="RoomID")
    private Room room;

    public LabTestCatalog() {
    }

    public String getLabTestId() {
        return labTestId;
    }

    public void setLabTestId(String labTestId) {
        this.labTestId = labTestId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}


