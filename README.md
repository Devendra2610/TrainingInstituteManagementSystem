# Training Institute Management System

A robust, full-stack Java web application engineered to seamlessly handle student lifecycles, internship placements, and highly secure online examinations.

## 🚀 Features

### 🎓 Student Lifecycle & Internship Module
*   **Profile Management**: Students can register, log in, and actively manage their personal details and CGPA.
*   **Dynamic Eligibility Filtering**: The Student Dashboard automatically filters available internships based on the student's exact CGPA against company-defined minimum requirements.
*   **Application Tracking**: Students can apply to internships (with strict duplicate-application prevention) and monitor real-time updates as Admins change their status (`APPLIED` → `SHORTLISTED` → `SELECTED`).

### 🏢 Company Management (Admin Dashboard)
*   **Company Onboarding**: Admins can register new partner companies into the system, automatically generating secure login credentials for them.
*   **Internship Posting**: Admins (and Companies) can post dynamic internship opportunities defining the Role, Description, Stipend, minimum CGPA eligibility, and strict Application Deadlines.
*   **Application Processing**: Admins can review all incoming student applications and update their statuses directly from an interactive, color-coded dashboard. 

### ⏱️ Secure Online Examination Engine
*   **Server-Controlled Timer**: Once a student's application is `SELECTED`, they unlock the Exam Interface. A server-enforced countdown timer guarantees absolute strictness regarding the exam duration.
*   **Zero-Latency Auto-Save**: Using asynchronous AJAX communication, every selected radio button (MCQ) or typed character (Subjective) is instantly and silently saved to the server session.
*   **Dynamic Navigation & Pagination**: Students enjoy a clean pagination UI with a numbered navigation map, "Next/Previous" buttons, and an intuitive "Mark for Review" toggling feature.

### 🛡️ Advanced Anti-Cheating & Auto-Submission
*   **Strict Single-Session Binding**: Concurrent `HttpSession` mapping ensures that a user can only ever be logged in from a single browser instance. Logging in elsewhere permanently invalidates the previous session.
*   **IP Interception**: The `AuthenticationFilter` binds the user's initial IP address upon login and enforces it on every subsequent protected request, eliminating session hijacking.
*   **Tab Switch Detection**: A `visibilitychange` DOM listener actively monitors if the student switches tabs (e.g., to Google an answer). If triggered, a hard `TAB_SWITCH_DETECTED` violation is logged directly into the `AuditLogs` database with their IP address.
*   **Flawless Auto-Submission**: If the exam timer expires, the frontend auto-submits. If the browser closes, the internet drops, or the session is forcibly invalidated, a custom `HttpSessionListener` swoops in to capture their last known autosaved answers and safely commits them directly to the database.

### 📝 Hybrid Evaluation Engine
*   **Instant MCQ Auto-Grading**: `ExamController` utilizes a massive SQL `JOIN` query to instantly match the student's submitted answers against the correct answers and automatically calculate their MCQ marks.
*   **Admin Subjective Evaluation**: Completed exams populate on the Admin Dashboard where the administrator can manually read typed descriptive answers, award points, recalculate the final total out of 100, and issue the final Internship Decision (`SELECTED` or `REJECTED`).

---

## 🛠️ Technology Stack

*   **Backend**: Java 8, Servlets, JSP (JavaServer Pages)
*   **Frontend**: HTML5, Vanilla CSS3, JavaScript (AJAX)
*   **Database**: MySQL 8.0 (with JDBC)
*   **Server/Build**: Apache Tomcat 7, Maven

---

## ⚙️ Setup & Installation

1.  **Database Configuration**:
    *   Open MySQL and execute the schema provided in `database.sql` to generate the `institute_db` database.
    *   Default credentials expected by the system: `root` / `Root`. (Modify `DBConnectionUtil.java` if yours differ).

2.  **Build & Run the Application**:
    *   Ensure you have Java 8 (`jdk1.8.0`) and Maven installed.
    *   Navigate to the project root directory in your terminal.
    *   Execute the following command to clean, compile, and launch the embedded Tomcat server:
        ```bash
        mvn clean tomcat7:run
        ```

3.  **Access the Application**:
    *   Open your browser and navigate to: `http://localhost:8080/login.jsp`
    *   **Default Admin Login**: `admin` / `admin123`
     *   **Default Stdent Login**: `student` / `student123`
