-- Script SQL tạo bảng Monitoring cho chức năng AI (RAG Access Log)
USE Diabetes;
GO

-- Bảng lưu nhật ký truy xuất dữ liệu bệnh nhân từ AI Tool (AI Patient Access Log)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ai_patient_access_log')
BEGIN
    CREATE TABLE ai_patient_access_log (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        queryLogId BIGINT NULL,
        patientId NVARCHAR(50) NOT NULL,
        dataType VARCHAR(100) NOT NULL,
        accessedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        question NVARCHAR(1000) NULL,
        latencyMs BIGINT NULL
    );

    CREATE INDEX IX_ai_patient_access_log_queryLogId ON ai_patient_access_log(queryLogId);
    CREATE INDEX IX_ai_patient_access_log_patientId ON ai_patient_access_log(patientId);
    CREATE INDEX IX_ai_patient_access_log_dataType ON ai_patient_access_log(dataType);
    CREATE INDEX IX_ai_patient_access_log_accessedAt ON ai_patient_access_log(accessedAt DESC);

    PRINT 'Created table ai_patient_access_log successfully.';
END
ELSE
BEGIN
    PRINT 'Table ai_patient_access_log already exists. Checking new columns...';

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ai_patient_access_log') AND name = 'question')
    BEGIN
        ALTER TABLE ai_patient_access_log ADD question NVARCHAR(1000) NULL;
        PRINT 'Added column question to ai_patient_access_log.';
    END
    ELSE
    BEGIN
        ALTER TABLE ai_patient_access_log ALTER COLUMN question NVARCHAR(1000) NULL;
        PRINT 'Altered column question to NVARCHAR(1000).';
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ai_patient_access_log') AND name = 'latencyMs')
    BEGIN
        ALTER TABLE ai_patient_access_log ADD latencyMs BIGINT NULL;
        PRINT 'Added column latencyMs to ai_patient_access_log.';
    END
END
GO
