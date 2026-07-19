USE Diabetes;
GO

-- 1. Đảm bảo Role 'DOC' tồn tại
IF NOT EXISTS (SELECT 1 FROM Role WHERE RoleID = 'DOC')
BEGIN
    INSERT INTO Role (RoleID, RoleName) VALUES ('DOC', 'Doctor');
END
GO

-- 2. Thêm dữ liệu Bác sĩ 1
IF NOT EXISTS (SELECT 1 FROM Account WHERE PhoneNumber = '0328938601')
BEGIN
    -- Mật khẩu giải mã bằng BCrypt của hash bên dưới là '123456'
    INSERT INTO Account (UserID, PhoneNumber, PasswordHash, RoleID, Status)
    VALUES ('DOC00001', '0328938601', '$2a$10$gA07W4vdzJFNa1G2cOOvye.H0D0LGM8vFubBXANGzPBKEgXOJNcxO', 'DOC', 'Active');

    INSERT INTO [Profile] (UserID, FullName, PhoneNumber, Address, Dob, Gender, RoomID, Specialty, ImageURL)
    VALUES (
        'DOC00001', 
        N'BS. Nguyễn Văn A', 
        '0328938601', 
        N'123 Đường Giải Phóng, Hà Nội', 
        '1980-05-15', 
        0, -- Nam
        (SELECT TOP 1 RoomID FROM Room WHERE RoomName = N'Endocrinology Clinic'), 
        N'Nội tiết - Tiểu đường', 
        NULL
    );
    PRINT 'Inserted Doctor: BS. Nguyễn Văn A (0328938601)';
END
GO

-- 3. Thêm dữ liệu Bác sĩ 2
IF NOT EXISTS (SELECT 1 FROM Account WHERE PhoneNumber = '0328938602')
BEGIN
    INSERT INTO Account (UserID, PhoneNumber, PasswordHash, RoleID, Status)
    VALUES ('DOC00002', '0328938602', '$2a$10$gA07W4vdzJFNa1G2cOOvye.H0D0LGM8vFubBXANGzPBKEgXOJNcxO', 'DOC', 'Active');

    INSERT INTO [Profile] (UserID, FullName, PhoneNumber, Address, Dob, Gender, RoomID, Specialty, ImageURL)
    VALUES (
        'DOC00002', 
        N'BS. Trần Thị B', 
        '0328938602', 
        N'456 Đường Nguyễn Huệ, Quận 1, TP. HCM', 
        '1985-08-20', 
        1, -- Nữ
        (SELECT TOP 1 RoomID FROM Room WHERE RoomName = N'Endocrinology Clinic'), 
        N'Nội tiết - Tiểu đường', 
        NULL
    );
    PRINT 'Inserted Doctor: BS. Trần Thị B (0328938602)';
END
GO

-- 4. Thêm dữ liệu Bác sĩ 3
IF NOT EXISTS (SELECT 1 FROM Account WHERE PhoneNumber = '0328938603')
BEGIN
    INSERT INTO Account (UserID, PhoneNumber, PasswordHash, RoleID, Status)
    VALUES ('DOC00003', '0328938603', '$2a$10$gA07W4vdzJFNa1G2cOOvye.H0D0LGM8vFubBXANGzPBKEgXOJNcxO', 'DOC', 'Active');

    INSERT INTO [Profile] (UserID, FullName, PhoneNumber, Address, Dob, Gender, RoomID, Specialty, ImageURL)
    VALUES (
        'DOC00003', 
        N'BS. Phạm Minh C', 
        '0328938603', 
        N'789 Đường Lê Duẩn, Hải Châu, Đà Nẵng', 
        '1990-12-10', 
        0, -- Nam
        (SELECT TOP 1 RoomID FROM Room WHERE RoomName = N'Endocrinology Clinic'), 
        N'Nội tiết - Tiểu đường', 
        NULL
    );
    PRINT 'Inserted Doctor: BS. Phạm Minh C (0328938603)';
END
GO
