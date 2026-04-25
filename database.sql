-- =================================================================================
-- Integrated Internship and Online Examination Management System Database Schema
-- =================================================================================

-- 1. Users Table (Handling Authentication & Authorization)
CREATE TABLE Users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STUDENT', 'COMPANY') NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- 2. Students Table (Extending Users for Student Profiles)
CREATE TABLE Students (
    student_id INT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    cgpa DECIMAL(3, 2) CHECK (cgpa >= 0.00 AND cgpa <= 10.00),
    resume_url VARCHAR(255),
    FOREIGN KEY (student_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- 3. Companies Table
CREATE TABLE Companies (
    company_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    industry VARCHAR(100),
    website VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- 4. Internships Table
CREATE TABLE Internships (
    internship_id INT PRIMARY KEY AUTO_INCREMENT,
    company_id INT NOT NULL,
    role_title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    stipend DECIMAL(10, 2),
    min_cgpa DECIMAL(3, 2) DEFAULT 0.00,
    deadline DATE NOT NULL,
    status ENUM('OPEN', 'CLOSED') DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES Companies(company_id) ON DELETE CASCADE
);

-- 5. Applications Table (Tracking Internship Applications)
CREATE TABLE Applications (
    application_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    internship_id INT NOT NULL,
    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('APPLIED', 'SHORTLISTED', 'REJECTED', 'SELECTED') DEFAULT 'APPLIED',
    UNIQUE (student_id, internship_id), -- Prevents duplicate applications
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (internship_id) REFERENCES Internships(internship_id) ON DELETE CASCADE
);

-- 6. Exams Table (Online Examination Configuration)
CREATE TABLE Exams (
    exam_id INT PRIMARY KEY AUTO_INCREMENT,
    internship_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    duration_minutes INT NOT NULL,
    total_marks INT NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    FOREIGN KEY (internship_id) REFERENCES Internships(internship_id) ON DELETE CASCADE
);

-- 7. Questions Table (MCQ & Subjective)
CREATE TABLE Questions (
    question_id INT PRIMARY KEY AUTO_INCREMENT,
    exam_id INT NOT NULL,
    question_text TEXT NOT NULL,
    question_type ENUM('MCQ', 'SUBJECTIVE') NOT NULL,
    option_a VARCHAR(255),
    option_b VARCHAR(255),
    option_c VARCHAR(255),
    option_d VARCHAR(255),
    correct_option CHAR(1), -- 'A', 'B', 'C', or 'D' for MCQ
    marks INT NOT NULL,
    FOREIGN KEY (exam_id) REFERENCES Exams(exam_id) ON DELETE CASCADE
);

-- 8. StudentExams Table (Tracking Exam Attempts and Results)
CREATE TABLE StudentExams (
    attempt_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    exam_id INT NOT NULL,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    marks_obtained INT DEFAULT 0,
    status ENUM('IN_PROGRESS', 'COMPLETED', 'EVALUATED') DEFAULT 'IN_PROGRESS',
    UNIQUE (student_id, exam_id), -- Prevents multiple attempts
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (exam_id) REFERENCES Exams(exam_id) ON DELETE CASCADE
);

-- 9. ExamAnswers Table (Auto-save and submission of answers)
CREATE TABLE ExamAnswers (
    answer_id INT PRIMARY KEY AUTO_INCREMENT,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_option CHAR(1), -- For MCQ
    subjective_answer TEXT, -- For Subjective
    marks_awarded INT DEFAULT 0,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (attempt_id, question_id),
    FOREIGN KEY (attempt_id) REFERENCES StudentExams(attempt_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES Questions(question_id) ON DELETE CASCADE
);

-- 10. AuditLogs Table (Security and Suspicious Activity Logs)
CREATE TABLE AuditLogs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE SET NULL
);

-- Indexes for performance (Reporting & Search)
CREATE INDEX idx_internship_cgpa ON Internships(min_cgpa);
CREATE INDEX idx_application_status ON Applications(status);
CREATE INDEX idx_student_exam_status ON StudentExams(status);
