--DROP trước
--IF DB_ID('Diabetes') IS NOT NULL
--   DROP DATABASE Diabetes;
--GO
CREATE DATABASE Diabetes;
GO

USE Diabetes;
GO

CREATE TABLE [Room] (
    [RoomID] INT IDENTITY(1,1) PRIMARY KEY,
    [RoomName] VARCHAR(50) NOT NULL UNIQUE
);

-- 1. Role
CREATE TABLE [Role] (
    RoleID VARCHAR(50) PRIMARY KEY,
    RoleName VARCHAR(100) NOT NULL UNIQUE
);

-- 2. User
CREATE TABLE [Account] (
    UserID VARCHAR(50) PRIMARY KEY,
    PhoneNumber NVARCHAR(50) NOT NULL UNIQUE, -- accountName
    PasswordHash VARCHAR(255) NOT NULL, -- password
    RoleID VARCHAR(50),
	Status VARCHAR(20) DEFAULT('Active') CHECK (Status in ('Active', 'Clocked'))
    FOREIGN KEY (RoleID) REFERENCES Role(RoleID) ON DELETE SET NULL ON UPDATE CASCADE
	-- "ON DELETE SET NULL": nếu ta xóa 1 dòng A bên bảng Role thì thuộc tính FK RoleID của các dòng ở bảng User
	--                       tham chiếu đến dòng A sẽ được sửa thành null
	-- "UPDATE CASCADE": nếu ta update 1 PK ở bên bảng Role (vd: 1 -> 3) thì thuộc tính FK RoleID của các dòng ở bảng User
	--                   tham chiếu đến dòng A cũng sẽ được sửa từ 1 thành 3
);

-- 3. Profile
CREATE TABLE [Profile] (
    UserID VARCHAR(50) PRIMARY KEY,
    FullName NVARCHAR(60) NOT NULL,
    PhoneNumber VARCHAR(15) unique,
    Address NVARCHAR(200),
    Dob DATE,
    Gender BIT, -- 0 -> male; 1 -> female
	[RoomID] INT NULL,
	Specialty NVARCHAR(60),
	ImageURL NVARCHAR(255) NULL,
    FOREIGN KEY (UserID) REFERENCES [Account](UserID) ON DELETE CASCADE  ON UPDATE CASCADE,
	FOREIGN KEY ([RoomID]) REFERENCES [Room]([RoomID]) ON DELETE SET NULL ON UPDATE CASCADE
);

-- 4. Patient
CREATE TABLE [Patient] (
    UserID VARCHAR(50) PRIMARY KEY,
	FullName NVARCHAR(60) NOT NULL,
    PhoneNumber VARCHAR(15) unique,
    Address NVARCHAR(200),
    Dob DATE,
    Gender BIT, -- 0 -> male; 1 -> female
    Height INT,
    Weight DECIMAL(5,2),
    Bloodgroup VARCHAR(3),
    PermanentMedicalHistory NVARCHAR(MAX),
    AllergyNotes NVARCHAR(MAX),
    SupervisorName NVARCHAR(90),
    SupervisorPhone VARCHAR(15),
	ImageURL NVARCHAR(255) NULL,
    FOREIGN KEY (UserID) REFERENCES [Account](UserID) ON DELETE CASCADE ON UPDATE CASCADE,
    CHECK (Bloodgroup IN ('A+','A-','B+','B-','AB+','AB-','O+','O-'))
);

CREATE TABLE PatientRoutine (
    UserID VARCHAR(50) PRIMARY KEY,
    BreakfastTime TIME,
    LunchTime TIME,
    DinnerTime TIME,

    WakeUpTime TIME,
    SleepTime TIME,

    FOREIGN KEY (UserID)
        REFERENCES Patient(UserID) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 5. ClinicalExamination
CREATE TABLE [ClinicalExamination] (
    ClinicalExamID VARCHAR(50) PRIMARY KEY,
    ExamDate DATETIME DEFAULT GETDATE(),
    MedicalHistory NVARCHAR(MAX), -- tiền sử bệnh
    DiagnosisNote NVARCHAR(MAX), -- chẩn đoán
    CancelReason NVARCHAR(MAX), -- lý do hủy
    NextAppointment DATETIME, -- lịch tái khám
    Status NVARCHAR(20) DEFAULT 'Pending'
        CHECK (Status IN ('Pending','InProgress','Completed','Cancelled')),
    PatientID VARCHAR(50) NOT NULL,
    DoctorID VARCHAR(50) NOT NULL,
    FOREIGN KEY (PatientID) REFERENCES Patient(UserID) ON UPDATE CASCADE,
    FOREIGN KEY (DoctorID) REFERENCES [Account](UserID)
);

-- 6. Symptoms Catalog
CREATE TABLE [Symptoms_Catalog] (
    SymptomID VARCHAR(50) PRIMARY KEY,
    SymptomName NVARCHAR(200) UNIQUE,
	Status BIT DEFAULT(1) -- 1 -> unclock , 0 -> lock
);

-- 7. ExamSymptom
CREATE TABLE [ExamSymptom] (
    ClinicalExamID VARCHAR(50),
    SymptomID VARCHAR(50),
    Note NVARCHAR(MAX),
    PRIMARY KEY (ClinicalExamID, SymptomID),
    FOREIGN KEY (ClinicalExamID) REFERENCES ClinicalExamination(ClinicalExamID) ON DELETE CASCADE,
    FOREIGN KEY (SymptomID) REFERENCES Symptoms_Catalog(SymptomID)
);

-- 8. Lab Test Catalog
CREATE TABLE [Lab_Test_Catalog] (
    LabTestID VARCHAR(50) PRIMARY KEY,
    TestName NVARCHAR(100) UNIQUE,
    Unit NVARCHAR(20), -- đơn vị mô tả
    Description NVARCHAR(MAX),
	RoomID INT,
	FOREIGN KEY (RoomID) REFERENCES Room(RoomID) ON DELETE CASCADE ON UPDATE CASCADE,
	Status BIT DEFAULT(1) -- 1 -> unclock , 0 -> lock
);

CREATE TABLE PatientType (
    PatientTypeID INT IDENTITY(1,1) PRIMARY KEY,

    TypeName NVARCHAR(50) NOT NULL,

    MinAge INT,

    MaxAge INT
);

CREATE TABLE IndicatorThreshold (
    ThresholdID INT IDENTITY(1,1) PRIMARY KEY,

    LabTestID VARCHAR(50) NOT NULL,
    PatientTypeID INT NOT NULL,

    MinValue DECIMAL(10,2),
    MaxValue DECIMAL(10,2),

    CreatedAt DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (LabTestID)
        REFERENCES Lab_Test_Catalog(LabTestID) ON DELETE CASCADE ON UPDATE CASCADE,

    FOREIGN KEY (PatientTypeID)
        REFERENCES PatientType(PatientTypeID)
);

-- 9. LabOrder
CREATE TABLE [LabOrder] (
    LabOrderID VARCHAR(50) PRIMARY KEY,
    Status NVARCHAR(20) DEFAULT 'Pending'
        CHECK (Status IN ('Pending','Completed','Cancelled')),
    ClinicalExamID VARCHAR(50), -- phiên khám
    FOREIGN KEY (ClinicalExamID) REFERENCES ClinicalExamination(ClinicalExamID) ON DELETE CASCADE
);

-- 10. LabResult
CREATE TABLE [LabResult] (
    LabResultID VARCHAR(50) PRIMARY KEY,
    Status NVARCHAR(20),
    Flag NVARCHAR(50), -- cờ cảnh báo chỉ số bất thường
    ResultValue DECIMAL(10,2),
    ReferenceRange NVARCHAR(50), -- chỉ số ngưỡng
    LabOrderID VARCHAR(50),
    LabTestID VARCHAR(50), 
    FOREIGN KEY (LabOrderID) REFERENCES LabOrder(LabOrderID) ON DELETE CASCADE,
    FOREIGN KEY (LabTestID) REFERENCES Lab_Test_Catalog(LabTestID)
);

-- 11. Prescription
CREATE TABLE [Prescription] (
    PrescriptionID VARCHAR(50) PRIMARY KEY,
    CreatedAt DATETIME DEFAULT GETDATE(),
    ClinicalExamID VARCHAR(50),
    FOREIGN KEY (ClinicalExamID) REFERENCES ClinicalExamination(ClinicalExamID)
);

-- 11.5. TreatmentPlan
CREATE TABLE [TreatmentPlan] (
    [PlanID] INT IDENTITY(1,1) PRIMARY KEY,
    [ClinicalExamID] VARCHAR(50) NOT NULL,
    [TreatmentGoal] NVARCHAR(MAX),
    [DietPlan] NVARCHAR(MAX),
    [ExercisePlan] NVARCHAR(MAX),
    [GlucoseMonitoringPlan] NVARCHAR(MAX),
    [CreatedAt] DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (ClinicalExamID) REFERENCES ClinicalExamination(ClinicalExamID) ON DELETE CASCADE
);

-- 12. Medication
CREATE TABLE [Medication] (
    MedicationID VARCHAR(50) PRIMARY KEY,
    MedicationName NVARCHAR(100) UNIQUE,
    Form NVARCHAR(50), -- dạng bào chế
    Concentration NVARCHAR(50), -- Nồng độ
    AdministrationRoute NVARCHAR(50), -- đường dùng
    UsageInstruction NVARCHAR(MAX), -- hướng dẫn sử dụng mặc định
	Status VARCHAR(20) DEFAULT('Active') CHECK (Status in ('Active', 'Clocked'))
);

-- 13. PrescriptionDetail
CREATE TABLE [PrescriptionDetail] (
    PrescriptionDetailID VARCHAR(50) PRIMARY KEY,
    PrescriptionID VARCHAR(50),
    MedicationID VARCHAR(50),
    Dosage NVARCHAR(50), -- liều lượng (mỗi lần dùng bn)
    TotalQuantity INT, -- tổng số thuốc cung cấp
    DurationDays INT, -- tổng số ngày sử dụng
    MedicationPlan NVARCHAR(MAX),
    StartDate DATE,
    EndDate DATE,
    FOREIGN KEY (PrescriptionID) REFERENCES Prescription(PrescriptionID) ON DELETE CASCADE,
    FOREIGN KEY (MedicationID) REFERENCES Medication(MedicationID)
);

CREATE TABLE MedicationTiming (
    TimingID INT IDENTITY(1,1) PRIMARY KEY,
    TimingName NVARCHAR(100)
);

CREATE TABLE PrescriptionTiming (
    PrescriptionTimingID BIGINT IDENTITY(1,1) PRIMARY KEY,

    PrescriptionDetailID VARCHAR(50) NOT NULL,
    TimingID INT NOT NULL,

    CONSTRAINT UQ_PrescriptionTiming
        UNIQUE(PrescriptionDetailID, TimingID),

    FOREIGN KEY (PrescriptionDetailID)
        REFERENCES PrescriptionDetail(PrescriptionDetailID)
        ON DELETE CASCADE,

    FOREIGN KEY (TimingID)
        REFERENCES MedicationTiming(TimingID)
);

-- 14. AI Assistant
CREATE TABLE [AI_Assistant] (
    AIAssistantID INT IDENTITY(1,1)PRIMARY KEY,
    AIName NVARCHAR(100) UNIQUE,
    Status NVARCHAR(50),
    ModelName VARCHAR(50)
);

CREATE TABLE PromptTemplate (
    TemplateID INT IDENTITY(1,1) PRIMARY KEY,

    TemplateName NVARCHAR(100) NOT NULL,

    SystemPrompt NVARCHAR(MAX),

    IsActive BIT DEFAULT 1,

    CreatedAt DATETIME DEFAULT GETDATE()
);

-- 15. Conversation
CREATE TABLE [AI_Conversation] (
    AIConversationID VARCHAR(50) PRIMARY KEY,
    CreatedAt DATETIME DEFAULT GETDATE(),
    PatientID VARCHAR(50),
    AIAssistantID INT,
    Topic NVARCHAR(150),
    FOREIGN KEY (PatientID) REFERENCES Patient(UserID) ON DELETE CASCADE,
    FOREIGN KEY (AIAssistantID) REFERENCES AI_Assistant(AIAssistantID) ON DELETE CASCADE
);

-- 16. Message
CREATE TABLE [AI_Message] (
    AIMessageID BIGINT IDENTITY(1,1)PRIMARY KEY,
    Content NVARCHAR(MAX),
    Sender VARCHAR(10) CHECK (Sender IN ('Patient','AI')),
    Time DATETIME DEFAULT GETDATE(),
    AIConversationID VARCHAR(50),
    FOREIGN KEY (AIConversationID) REFERENCES AI_Conversation(AIConversationID) ON DELETE CASCADE
);

-- 17. Notification
CREATE TABLE [AI_Reminder] (
    AIReminderID BIGINT IDENTITY(1,1)PRIMARY KEY,
    Title NVARCHAR(50),
    Message NVARCHAR(MAX),
    ScheduledTime DATETIME,
    IsRead BIT DEFAULT 0,
    AIAssistantID INT,
    PatientID VARCHAR(50),
    TimingID INT,
    FOREIGN KEY (PatientID) REFERENCES Patient(UserID) ON DELETE CASCADE,
    FOREIGN KEY (AIAssistantID) REFERENCES AI_Assistant(AIAssistantID) ON DELETE CASCADE,
    FOREIGN KEY (TimingID) REFERENCES MedicationTiming(TimingID)
);

------------------  INSERT  ----------------------------------------
INSERT INTO [Role] (RoleID, RoleName)
VALUES
    ('AD', 'Admin'),
    ('PAT', 'Patient'),
    ('DOC', 'Doctor');

INSERT INTO PatientType (TypeName, MinAge, MaxAge)
VALUES
    ('Adult', 18, 39),
    ('Middle-aged', 40, 64),
    ('Elderly', 65, 120),
    ('Pregnant', NULL, NULL);

INSERT INTO [Room] ([RoomName])
VALUES
('Endocrinology Clinic'),
('Laboratory');

INSERT INTO [Symptoms_Catalog] (SymptomID, SymptomName, Status)
VALUES 
('SYM001', N'Khát nước quá mức (Polydipsia)', 1),
('SYM002', N'Đi tiểu nhiều lần (Polyuria)', 1),
('SYM003', N'Đói dữ dội (Polyphagia)', 1),
('SYM004', N'Sụt cân không rõ nguyên nhân', 1),
('SYM005', N'Mệt mỏi, uể oải', 1),
('SYM006', N'Mờ mắt', 1),
('SYM007', N'Vết thương lâu lành', 1);

INSERT INTO [Lab_Test_Catalog] (LabTestID, TestName, Unit, Description, RoomID, Status)
VALUES 
('LAB001', N'Đường huyết lúc đói (FPG)', 'mg/dL', N'Đo lượng đường trong máu sau khi nhịn ăn 8h', 2, 1),
('LAB002', N'HbA1c', '%', N'Đo đường huyết trung bình trong 2-3 tháng qua', 2, 1),
('LAB003', N'Nghiệm pháp dung nạp Glucose (OGTT)', 'mg/dL', N'Đánh giá khả năng chuyển hóa đường của cơ thể', 2, 1),
('LAB004', N'Đường huyết ngẫu nhiên', 'mg/dL', N'Đo đường huyết tại thời điểm bất kỳ', 2, 1);

INSERT INTO [Medication] (MedicationID, MedicationName, Form, Concentration, AdministrationRoute, UsageInstruction, Status)
VALUES 
('MED001', 'Metformin', N'Viên nén', '500mg', N'Đường uống', N'Uống sau khi ăn', 'Active'),
('MED002', 'Gliclazide', N'Viên nén', '30mg', N'Đường uống', N'Uống trước bữa ăn sáng', 'Active'),
('MED003', 'Insulin Glargine', N'Bút tiêm', '100IU/ml', N'Tiêm dưới da', N'Tiêm vào cùng một thời điểm mỗi ngày', 'Active'),
('MED004', 'Sitagliptin', N'Viên nén', '100mg', N'Đường uống', N'Uống một lần mỗi ngày', 'Active');
