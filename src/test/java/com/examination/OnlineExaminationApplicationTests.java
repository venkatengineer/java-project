package com.examination;

import com.examination.dto.ExamSubmissionDto;
import com.examination.dto.UserDto;
import com.examination.entity.*;
import com.examination.repository.UserRepository;
import com.examination.service.ExamService;
import com.examination.service.SubmissionService;
import com.examination.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OnlineExaminationApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private ExamService examService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
        assertNotNull(userService);
        assertNotNull(examService);
        assertNotNull(submissionService);
    }

    @Test
    @DisplayName("Test BCrypt password hashing and user creation")
    void testUserCreationAndBCrypt() {
        UserDto dto = new UserDto();
        dto.setName("Unit Test Student");
        dto.setEmail("unittest@example.com");
        dto.setPassword("secretPassword");

        User created = userService.createUser(dto, Role.STUDENT);
        assertNotNull(created.getId());
        assertEquals("Unit Test Student", created.getName());
        assertEquals(Role.STUDENT, created.getRole());

        // Password should be hashed with BCrypt, never plain text
        assertNotEquals("secretPassword", created.getPassword());
        assertTrue(passwordEncoder.matches("secretPassword", created.getPassword()));
    }

    @Test
    @DisplayName("Test examination submission, grading, and duplicate prevention")
    void testExamGradingAndDuplicatePrevention() {
        User student = userRepository.findByEmail("carol.student@exam.com").orElseThrow();
        Exam exam = examService.getAllExams().getFirst();

        // Check if already attempted Exam 1 (Carol hasn't attempted Exam 1)
        if (!submissionService.hasStudentAttempted(student, exam)) {
            ExamSubmissionDto submissionDto = new ExamSubmissionDto(exam.getId());
            // Supply answers for the first two questions
            Question q1 = exam.getQuestions().get(0);
            submissionDto.setAnswers(Map.of(q1.getId(), q1.getCorrectAnswer()));

            Submission sub = submissionService.submitExam(exam.getId(), student, submissionDto);
            assertNotNull(sub.getId());
            assertTrue(sub.getScore() >= q1.getMarks());
            assertEquals("COMPLETED", sub.getStatus());

            // Duplicate attempt check
            assertTrue(submissionService.hasStudentAttempted(student, exam));
            assertThrows(Exception.class, () -> {
                submissionService.submitExam(exam.getId(), student, submissionDto);
            });
        }
    }
}
