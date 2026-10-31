package com.examination.controller;

import com.examination.dto.ExamSubmissionDto;
import com.examination.dto.PasswordChangeDto;
import com.examination.dto.StudentExamQuestionDto;
import com.examination.entity.Exam;
import com.examination.entity.Submission;
import com.examination.entity.User;
import com.examination.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final UserService userService;
    private final ExamService examService;
    private final QuestionService questionService;
    private final SubmissionService submissionService;
    private final ResultService resultService;

    public StudentController(UserService userService,
                             ExamService examService,
                             QuestionService questionService,
                             SubmissionService submissionService,
                             ResultService resultService) {
        this.userService = userService;
        this.examService = examService;
        this.questionService = questionService;
        this.submissionService = submissionService;
        this.resultService = resultService;
    }

    // Student Dashboard
    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User student = userService.getUserById(userDetails.getId());
        List<Exam> availableExams = examService.getAvailableExams();
        List<Submission> mySubmissions = submissionService.getSubmissionsByStudent(student);

        // Map examId -> submission for quick lookup
        Map<Long, Submission> attemptedMap = new HashMap<>();
        for (Submission s : mySubmissions) {
            attemptedMap.put(s.getExam().getId(), s);
        }

        long completedCount = mySubmissions.size();
        long availableCount = availableExams.stream()
                .filter(e -> !attemptedMap.containsKey(e.getId()))
                .count();

        double avgPercentage = submissionService.getStudentAveragePercentage(student.getId());

        model.addAttribute("availableCount", availableCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("upcomingCount", availableCount);
        model.addAttribute("avgPercentage", avgPercentage);
        model.addAttribute("recentSubmissions", mySubmissions.stream().limit(5).toList());
        model.addAttribute("availableExams", availableExams);
        model.addAttribute("attemptedMap", attemptedMap);
        model.addAttribute("currentUser", userDetails);
        return "student/dashboard";
    }

    // Available Examinations
    @GetMapping("/exams")
    public String availableExams(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User student = userService.getUserById(userDetails.getId());
        List<Exam> exams = examService.getAvailableExams();
        List<Submission> mySubmissions = submissionService.getSubmissionsByStudent(student);

        Map<Long, Submission> attemptedMap = new HashMap<>();
        for (Submission s : mySubmissions) {
            attemptedMap.put(s.getExam().getId(), s);
        }

        model.addAttribute("exams", exams);
        model.addAttribute("attemptedMap", attemptedMap);
        model.addAttribute("currentUser", userDetails);
        return "student/available-exams";
    }

    // Exam Instructions
    @GetMapping("/exams/{id}/instructions")
    public String examInstructions(@PathVariable("id") Long id,
                                   Model model,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        Exam exam = examService.getExamById(id);
        User student = userService.getUserById(userDetails.getId());

        // Check if student already attempted
        Optional<Submission> existing = submissionService.getSubmissionForStudentAndExam(student, exam);
        if (existing.isPresent()) {
            return "redirect:/student/results/" + existing.get().getId();
        }

        model.addAttribute("exam", exam);
        model.addAttribute("questionCount", questionService.countQuestionsByExam(exam));
        model.addAttribute("currentUser", userDetails);
        return "student/exam-instructions";
    }

    // Distraction-Free Exam Screen
    @GetMapping("/exams/{id}/attend")
    public String attendExam(@PathVariable("id") Long id,
                             Model model,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        Exam exam = examService.getExamById(id);
        User student = userService.getUserById(userDetails.getId());

        // Check if student already completed this exam
        Optional<Submission> existing = submissionService.getSubmissionForStudentAndExam(student, exam);
        if (existing.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "You have already completed this examination.");
            return "redirect:/student/results/" + existing.get().getId();
        }

        // Securely fetch questions WITHOUT correct answers
        List<StudentExamQuestionDto> questions = questionService.getStudentQuestions(exam);
        if (questions.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "This examination currently has no questions.");
            return "redirect:/student/exams";
        }

        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("durationMinutes", exam.getDuration());
        model.addAttribute("currentUser", userDetails);
        return "student/attend-exam";
    }

    // Submit Examination
    @PostMapping("/exams/{id}/submit")
    public String submitExam(@PathVariable("id") Long id,
                             @RequestParam Map<String, String> formParams,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User student = userService.getUserById(userDetails.getId());

        ExamSubmissionDto submissionDto = new ExamSubmissionDto(id);
        Map<Long, String> answers = new HashMap<>();

        for (Map.Entry<String, String> entry : formParams.entrySet()) {
            if (entry.getKey().startsWith("question_")) {
                try {
                    Long questionId = Long.parseLong(entry.getKey().substring("question_".length()));
                    String chosen = entry.getValue();
                    if (chosen != null && !chosen.trim().isEmpty()) {
                        answers.put(questionId, chosen.trim().toUpperCase());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        submissionDto.setAnswers(answers);

        try {
            Submission submission = submissionService.submitExam(id, student, submissionDto);
            redirectAttributes.addFlashAttribute("successMessage", "Examination submitted successfully!");
            return "redirect:/student/results/" + submission.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/exams";
        }
    }

    // Examination Results View
    @GetMapping("/results/{id}")
    public String viewResult(@PathVariable("id") Long id,
                             Model model,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Submission submission = submissionService.getSubmissionById(id);
        User student = userService.getUserById(userDetails.getId());

        // Ensure student can only view their own result
        if (!submission.getStudent().getId().equals(student.getId())) {
            return "redirect:/student/history?accessDenied=true";
        }

        var answers = resultService.getDetailedAnswers(submission);
        model.addAttribute("submission", submission);
        model.addAttribute("exam", submission.getExam());
        model.addAttribute("answers", answers);
        model.addAttribute("currentUser", userDetails);
        return "student/results";
    }

    // Examination History / Results List
    @GetMapping({"/history", "/results"})
    public String examinationHistory(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User student = userService.getUserById(userDetails.getId());
        List<Submission> submissions = submissionService.getSubmissionsByStudent(student);

        model.addAttribute("submissions", submissions);
        model.addAttribute("currentUser", userDetails);
        return "student/exam-history";
    }

    // Student Profile
    @GetMapping("/profile")
    public String viewProfile(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User student = userService.getUserById(userDetails.getId());
        model.addAttribute("user", student);
        if (!model.containsAttribute("passwordDto")) {
            model.addAttribute("passwordDto", new PasswordChangeDto());
        }
        model.addAttribute("currentUser", userDetails);
        return "student/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("name") String name,
                                @RequestParam("email") String email,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.updateProfile(userDetails.getId(), name, email);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@Valid @ModelAttribute("passwordDto") PasswordChangeDto passwordDto,
                                 BindingResult result,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password validation error.");
            return "redirect:/student/profile";
        }
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password and confirmation password do not match.");
            return "redirect:/student/profile";
        }
        try {
            userService.changePassword(userDetails.getId(), passwordDto.getCurrentPassword(), passwordDto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/profile";
    }
}
