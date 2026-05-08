-- ============================================================
-- Employee Management System - MySQL Schema
-- Auto-Increment PKs + Foreign Keys + Proper Relational Design
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- Create tables only if they do not exist

-- ============================================================
-- USERS (root table — no FK dependencies)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(255),
    mobile      VARCHAR(50),
    address     TEXT,
    role        ENUM('ADMIN','EMPLOYEE','USER') NOT NULL DEFAULT 'USER',
    imageUrl    VARCHAR(512),
    skills      TEXT,
    jobRole     VARCHAR(255),
    companyInfo TEXT,
    createdAt   DATETIME,
    updatedAt   DATETIME
);

-- ============================================================
-- SHIFTS (independent — no FK dependencies)
-- ============================================================
CREATE TABLE IF NOT EXISTS shifts (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    shiftName   VARCHAR(100),
    startTime   TIME,
    endTime     TIME,
    description VARCHAR(512)
);

-- ============================================================
-- JOBS (independent — no FK dependencies)
-- ============================================================
CREATE TABLE IF NOT EXISTS jobs (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(255),
    department          VARCHAR(255),
    location            VARCHAR(255),
    type                VARCHAR(100),
    description         TEXT,
    keyResponsibilities TEXT,
    minSalary           INT,
    maxSalary           INT,
    requiredSkills      TEXT,
    createdAt           DATETIME,
    isActive            BOOLEAN DEFAULT TRUE
);

-- ============================================================
-- ATTENDANCE (FK → users.id)
-- ============================================================
CREATE TABLE IF NOT EXISTS attendance (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    employeeId   BIGINT NOT NULL,
    date         DATE,
    checkInTime  DATETIME,
    checkOutTime DATETIME,
    status       VARCHAR(50),
    workingHours DOUBLE,
    CONSTRAINT fk_attendance_user
        FOREIGN KEY (employeeId) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- LEAVES (FK → users.id for employee AND approver)
-- ============================================================
CREATE TABLE IF NOT EXISTS leaves (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    employeeId       BIGINT NOT NULL,
    leaveType        VARCHAR(100),
    startDate        DATE,
    endDate          DATE,
    reason           TEXT,
    status           VARCHAR(50) DEFAULT 'PENDING',
    approverId       BIGINT,
    approvedAt       DATETIME,
    approverComments TEXT,
    createdAt        DATETIME,
    updatedAt        DATETIME,
    numberOfDays     INT,
    CONSTRAINT fk_leave_employee
        FOREIGN KEY (employeeId) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_approver
        FOREIGN KEY (approverId) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- ASSETS (FK → users.id)
-- ============================================================
CREATE TABLE IF NOT EXISTS assets (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    employeeId   BIGINT,
    assetName    VARCHAR(255),
    assetType    VARCHAR(100),
    serialNumber VARCHAR(255),
    status       VARCHAR(100),
    assignedDate DATETIME,
    returnDate   DATETIME,
    conditions   VARCHAR(255),
    description  TEXT,
    remarks      TEXT,
    createdAt    DATETIME,
    updatedAt    DATETIME,
    CONSTRAINT fk_asset_employee
        FOREIGN KEY (employeeId) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- EMPLOYEE_SHIFTS (FK → users.id + shifts.id)
-- ============================================================
CREATE TABLE IF NOT EXISTS employee_shifts (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    employeeId BIGINT NOT NULL,
    shiftId    BIGINT NOT NULL,
    startDate  DATE,
    endDate    DATE,
    CONSTRAINT fk_empshift_employee
        FOREIGN KEY (employeeId) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_empshift_shift
        FOREIGN KEY (shiftId) REFERENCES shifts(id) ON DELETE CASCADE
);

-- ============================================================
-- JOB_APPLICATIONS (FK → jobs.id + users.id)
-- ============================================================
CREATE TABLE IF NOT EXISTS job_applications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    jobId       BIGINT NOT NULL,
    employeeId  BIGINT NOT NULL,
    resumeUrl   VARCHAR(512),
    status      VARCHAR(50) DEFAULT 'PENDING',
    appliedDate DATETIME,
    CONSTRAINT fk_app_job
        FOREIGN KEY (jobId) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_employee
        FOREIGN KEY (employeeId) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- COMPANY (singleton config table — no FKs)
-- ============================================================
CREATE TABLE IF NOT EXISTS company (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(255),
    foundedYear       INT,
    mission           TEXT,
    vision            TEXT,
    companyValues     LONGTEXT,
    locations         LONGTEXT,
    perks             LONGTEXT,
    testimonials      LONGTEXT,
    cultureHighlights LONGTEXT,
    faqs              LONGTEXT,
    contactInfo       LONGTEXT
);

SET FOREIGN_KEY_CHECKS = 1;
