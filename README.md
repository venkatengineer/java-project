# Online Examination System
## Academic Full-Stack Web Application (Java 21, Spring Boot 3, Thymeleaf & MySQL)

An enterprise-grade, modern, and aesthetically calming Online Examination and Assessment System built with **Java 21**, **Spring Boot 3.3.4**, **Thymeleaf**, **Spring Security 6**, **Spring Data JPA / Hibernate**, and **MySQL 8**.

---

## 1. Executive Summary & Design System

The platform is designed around modern SaaS paradigms (inspired by Linear and Apple interfaces) featuring a **soft pastel lavender-purple design system**:

### Color Palette
- **Primary Purple:** `#8B78C8` (Primary buttons, accents, active states)
- **Deep Purple:** `#6955A8` (Headings, primary brand iconography)
- **Lavender:** `#EAE4FA` (Badge backgrounds, soft containers)
- **Light Lavender:** `#F4F0FC` (Card fills, table headers)
- **Background:** `#F8F7FC` (Subtle pastel page canvas)
- **White:** `#FFFFFF` (Surface cards)
- **Soft Mint:** `#E5F3EC` / Dark: `#2D8A5E` (Success alerts, passed badges, faculty cards)
- **Soft Pink:** `#F8E8F1` / Dark: `#D45B94` (Failed badges, danger buttons)
- **Soft Blue:** `#E7EFFB` / Dark: `#3F72AF` (Student badges, calendar widgets)
- **Soft Yellow:** `#FFF3D9` / Dark: `#B8860B` (Curriculum subjects, warning alerts)
- **Main Text:** `#302B40` | **Secondary Text:** `#858095` | **Borders:** `#ECE8F3`

### Visual Highlights
- Soft rounded corners (14px–20px)
- Ultra-subtle elevation shadows (`0 4px 20px rgba(105, 85, 168, 0.06)`)
- Rounded outline icons placed inside softly colored rounded-square containers
- Distraction-free examination view with live countdown timer and question navigator grid
- Instant server-side evaluation with question-by-question breakdown

---

## 2. System Architecture

```
                    ┌─────────────────────────┐
                    │  Thymeleaf Web UI (MVC) │
                    │  HTML5 / CSS3 / JS ES6  │
                    └────────────┬────────────┘
                                 │ HTTP / Form Submit / Session Cookies
                                 ▼
                    ┌─────────────────────────┐
                    │    Spring Security 6    │
                    │ Role-Based Auth Filter  │
                    └────────────┬────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         ▼                       ▼                       ▼
┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
│  AdminController │   │TeacherController │   │StudentController │
└────────┬─────────┘   └────────┬─────────┘   └────────┬─────────┘
         │                      │                      │
         └──────────────────────┼──────────────────────┘
                                ▼
                    ┌─────────────────────────┐
                    │    Service Layer        │
                    │ UserService, ExamService│
                    │ SubmissionService (Tx)  │
                    │ QuestionService, Result │
                    └────────────┬────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │    Spring Data JPA      │
                    │ Hibernate ORM Entities  │
                    └────────────┬────────────┘
                                 │ JDBC Connection (Port 3306)
                                 ▼
                    ┌─────────────────────────┐
                    │  MySQL 8 / MariaDB DB   │
                    │online_examination_system│
                    └─────────────────────────┘
```

---

## 3. Pre-Seeded Demonstration Accounts

The database automatically initializes upon first run (`DataInitializer.java`) with realistic data:

| Portal | Role | Email Address | Password | Permissions |
|---|---|---|---|---|
| **Admin Portal** | `ADMIN` | `admin@exam.com` | `admin123` | Full system CRUD (Users, Subjects, Exams, Global Results) |
| **Teacher Portal** | `TEACHER` | `sarah.teacher@exam.com` | `teacher123` | Exam authoring, Question Bank, Submissions review |
| **Teacher Portal** | `TEACHER` | `robert.teacher@exam.com` | `teacher123` | Exam authoring, Question Bank, Submissions review |
| **Student Portal** | `STUDENT` | `alice.student@exam.com` | `student123` | Exam candidate (Attempted Exam 1 - Score 9/10) |
| **Student Portal** | `STUDENT` | `bob.student@exam.com` | `student123` | Exam candidate (Attempted Exam 1 - Score 7/10) |
| **Student Portal** | `STUDENT` | `carol.student@exam.com` | `student123` | Exam candidate (Attempted Exam 2 - Score 8/10) |
| **Student Portal** | `STUDENT` | `david.student@exam.com` | `student123` | Exam candidate (Ready to take new exams) |
| **Student Portal** | `STUDENT` | `emma.student@exam.com` | `student123` | Exam candidate (Attempted Exam 2 - Score 10/10) |

---

## 4. Database Schema

Database Name: `online_examination_system`

### Tables
1. **`users`**
   - `id` (BIGINT AUTO_INCREMENT PRIMARY KEY)
   - `name` (VARCHAR(100) NOT NULL)
   - `email` (VARCHAR(120) UNIQUE NOT NULL)
   - `password` (VARCHAR(255) NOT NULL, BCrypt hashed)
   - `role` (VARCHAR(20) NOT NULL: `ADMIN`, `TEACHER`, `STUDENT`)
   - `created_at` (DATETIME NOT NULL)

2. **`subjects`**
   - `id` (BIGINT AUTO_INCREMENT PRIMARY KEY)
   - `subject_name` (VARCHAR(150) UNIQUE NOT NULL)
   - `description` (TEXT)

3. **`exams`**
   - `id` (BIGINT AUTO_INCREMENT PRIMARY KEY)
   - `title` (VARCHAR(200) NOT NULL)
   - `description` (TEXT)
   - `subject_id` (FK -> `subjects.id`)
   - `teacher_id` (FK -> `users.id`)
   - `duration` (INT NOT NULL, in minutes)
   - `total_marks` (INT NOT NULL)
   - `start_time` (DATETIME)
   - `end_time` (DATETIME)
   - `status` (VARCHAR(30) NOT NULL: `DRAFT`, `PUBLISHED`, `COMPLETED`)

4. **`questions`**
   - `id` (BIGINT AUTO_INCREMENT PRIMARY KEY)
   - `exam_id` (FK -> `exams.id`)
   - `question_text` (TEXT NOT NULL)
   - `option_a` (TEXT NOT NULL)
   - `option_b` (TEXT NOT NULL)
   - `option_c` (TEXT NOT NULL)
   - `option_d` (TEXT NOT NULL)
   - `correct_answer` (VARCHAR(5) NOT NULL: `A`, `B`, `C`, `D`)
   - `marks` (INT NOT NULL DEFAULT 1)

5. **`submissions`**
   - `id` (BIGINT AUTO_INCREMENT PRIMARY KEY)
   - `student_id` (FK -> `users.id`)
   - `exam_id` (FK -> `exams.id`)
   - `score` (INT NOT NULL)
   - `submitted_at` (DATETIME NOT NULL)
   - `status` (VARCHAR(30) NOT NULL: `COMPLETED`)

6. **`student_answers`**
   - `id` (BIGINT AUTO_INCREMENT PRIMARY KEY)
   - `submission_id` (FK -> `submissions.id`)
   - `question_id` (FK -> `questions.id`)
   - `selected_answer` (VARCHAR(5): `A`, `B`, `C`, `D` or NULL if skipped)

---

## 5. Security & Academic Assessment Integrity

- **Role-Based Authentication:** Spring Security intercepts URL patterns (`/admin/**`, `/teacher/**`, `/student/**`). Users cannot access another role's dashboard simply by modifying the URL.
- **Server-Side Grading:** Correct answers are **strictly excluded** from student-facing payloads (`StudentExamQuestionDto`). Answer keys are never rendered in client DOM or JSON prior to submission.
- **Duplicate Attempt Prevention:** Checked at service and database levels. Students cannot resubmit completed examinations.
- **Transactional Evaluation:** `@Transactional` ensures atomicity across submission logging and individual answer records.
- **Live Countdown Timer:** Displays remaining time with visual alert (< 5 mins) and triggers automatic paper submission upon expiration.
- **Password Security:** All passwords stored using **BCrypt** with high work factor.

---

## 6. Installation & Execution Guide

### Option A: Running via Docker (Recommended for Windows & Any Host)
Docker completely removes the need to manually install Java 21 or MySQL on the host machine. The containers package **Eclipse Temurin Java 21 JRE** and **MySQL 8.0**.

#### On Windows:
1. Ensure **Docker Desktop for Windows** is installed and running.
2. Open Command Prompt or PowerShell in the project directory:
   ```cmd
   docker compose up --build -d
   ```
   *Or simply double-click / execute:*
   ```cmd
   docker-run.bat
   ```
3. Open `http://localhost:8080` in your web browser.

#### On Linux / macOS:
```bash
./docker-run.sh
# or
docker compose up --build -d
```

#### Viewing Logs & Stopping:
- **View application logs:** `docker compose logs -f app`
- **View database logs:** `docker compose logs -f mysql`
- **Stop containers:** `docker compose down`

---

### Option B: Running Directly on Host (Without Docker)

#### Prerequisites
- **Java:** JDK 21 *(Note: If your host has Java 17, running `java -jar` directly will cause `UnsupportedClassVersionError: class file version 65.0`. Use Docker or set your `JAVA_HOME` to Java 21)*.
- **Database:** MySQL 8 or MariaDB on port 3306.
- **Build Tool:** Maven 3.9+ (or included `./mvnw` / `mvnw.cmd`).

#### Database Setup
Log in to MySQL / MariaDB:
```sql
CREATE DATABASE IF NOT EXISTS online_examination_system;
CREATE USER IF NOT EXISTS 'exam_user'@'localhost' IDENTIFIED BY 'exam_password';
GRANT ALL PRIVILEGES ON online_examination_system.* TO 'exam_user'@'localhost';
FLUSH PRIVILEGES;
```

#### Starting the App
```bash
mvn spring-boot:run
# or on Windows:
mvnw.cmd spring-boot:run
```
Or run the JAR with Java 21:
```bash
java -jar target/online-examination-system-1.0.0.jar
```

---

## 7. URL Route Directory

### Public Routes
- `GET /` - Platform Landing Page with role portal selectors
- `GET /login/admin` - Admin authentication console
- `GET /login/teacher` - Teacher/faculty login portal
- `GET /login/student` - Student login portal
- `GET /register/student` - Student self-registration form
- `POST /register/student` - Registration handler

### Admin Routes (`ROLE_ADMIN`)
- `GET /admin/dashboard` - Platform metrics, total counts & activity
- `GET /admin/students` - Student directory & search
- `POST /admin/students/add` - Register new student
- `POST /admin/students/edit/{id}` - Modify student details
- `POST /admin/students/delete/{id}` - Remove student record
- `GET /admin/teachers` - Faculty registry & search
- `POST /admin/teachers/add` - Register new teacher
- `POST /admin/teachers/edit/{id}` - Modify teacher
- `POST /admin/teachers/delete/{id}` - Remove teacher
- `GET /admin/subjects` - Curriculum subject list
- `POST /admin/subjects/add` - Add subject
- `POST /admin/subjects/edit/{id}` - Update subject
- `POST /admin/subjects/delete/{id}` - Delete subject
- `GET /admin/exams` - All platform examinations
- `POST /admin/exams/delete/{id}` - Remove examination
- `GET /admin/results` - Global candidate submission logs
- `GET /admin/results/{id}` - Comprehensive question-by-question candidate analysis
- `GET /admin/profile` - Admin settings & password change

### Teacher Routes (`ROLE_TEACHER`)
- `GET /teacher/dashboard` - Faculty metrics & recent submissions
- `GET /teacher/exams` - Authored examinations
- `GET /teacher/exams/create` - Examination creator form
- `POST /teacher/exams/create` - Save examination
- `GET /teacher/exams/{id}/edit` - Modify examination
- `POST /teacher/exams/{id}/edit` - Update examination
- `POST /teacher/exams/{id}/delete` - Delete examination
- `GET /teacher/exams/{id}/questions` - Question bank authoring
- `POST /teacher/exams/{id}/questions/add` - Add MCQ question with 4 options
- `POST /teacher/questions/{id}/edit` - Update MCQ question
- `POST /teacher/questions/{id}/delete` - Remove question
- `GET /teacher/submissions` - Student test submissions
- `GET /teacher/submissions/{id}` - Detailed candidate answer key review
- `GET /teacher/profile` - Faculty profile & password change

### Student Routes (`ROLE_STUDENT`)
- `GET /student/dashboard` - Overview of upcoming, completed & available exams
- `GET /student/exams` - Available examinations catalog
- `GET /student/exams/{id}/instructions` - Exam briefing, rules & guidelines
- `GET /student/exams/{id}/attend` - Distraction-free examination interface
- `POST /student/exams/{id}/submit` - Submit answers for automatic evaluation
- `GET /student/results/{id}` - Instant score report & answer explanation
- `GET /student/history` - Archive of all completed examinations
- `GET /student/profile` - Student personal profile & password change

---

## 8. Verification & Test Execution

Run the automated integration test suite:
```bash
python3 scratch/test_e2e.py
```
Or run the JUnit tests:
```bash
mvn test
```

All tests verify authentication, authorization, CRUD operations, distraction-free examination taking, timer operation, duplicate prevention, and server-side grading.
