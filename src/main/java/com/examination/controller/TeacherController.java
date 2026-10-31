package com.examination.controller;

import com.examination.dto.ExamDto;
import com.examination.dto.PasswordChangeDto;
import com.examination.dto.QuestionDto;
import com.examination.entity.Exam;
import com.examination.entity.Question;
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

import java.util.List;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final UserService userService;
    private final SubjectService subjectService;
    private final ExamService examService;
    private final QuestionService questionService;
    private final SubmissionService submissionService;
    private final ResultService resultService;

    public TeacherController(UserService userService,
                             SubjectService subjectService,
                             ExamService examService,
                             QuestionService questionService,
                             SubmissionService submissionService,
                             ResultService resultService) {
        this.userService = userService;
        this.subjectService = subjectService;
        this.examService = examService;
        this.questionService = questionService;
        this.submissionService = submissionService;
        this.resultService = resultService;
    }

    // Teacher Dashboard
    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User teacher = userService.getUserById(userDetails.getId());
        model.addAttribute("totalExams", examService.countTeacherExams(teacher));
        model.addAttribute("totalQuestions", questionService.countQuestionsByTeacher(teacher.getId()));
        model.addAttribute("totalSubmissions", submissionService.countSubmissionsByTeacher(teacher.getId()));
        model.addAttribute("recentSubmissions", submissionService.getRecentTeacherSubmissions(teacher.getId(), 5));
        model.addAttribute("myExams", examService.getExamsByTeacher(teacher));
        model.addAttribute("currentUser", userDetails);
        return "teacher/dashboard";
    }

    // My Examinations
    @GetMapping("/exams")
    public String listExams(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User teacher = userService.getUserById(userDetails.getId());
        model.addAttribute("exams", examService.getExamsByTeacher(teacher));
        model.addAttribute("currentUser", userDetails);
        return "teacher/examinations";
    }

    // Create Examination form
    @GetMapping("/exams/create")
    public String showCreateExamForm(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (!model.containsAttribute("examDto")) {
            ExamDto dto = new ExamDto();
            dto.setDuration(30);
            dto.setTotalMarks(10);
            model.addAttribute("examDto", dto);
        }
        model.addAttribute("subjects", subjectService.getAllSubjects());
        model.addAttribute("currentUser", userDetails);
        return "teacher/create-exam";
    }

    @PostMapping("/exams/create")
    public String createExam(@Valid @ModelAttribute("examDto") ExamDto examDto,
                             BindingResult result,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("subjects", subjectService.getAllSubjects());
            model.addAttribute("currentUser", userDetails);
            return "teacher/create-exam";
        }
        User teacher = userService.getUserById(userDetails.getId());
        Exam created = examService.createExam(examDto, teacher);
        redirectAttributes.addFlashAttribute("successMessage", "Examination created successfully! You can now add questions.");
        return "redirect:/teacher/exams/" + created.getId() + "/questions";
    }

    // Edit Examination
    @GetMapping("/exams/{id}/edit")
    public String showEditExamForm(@PathVariable("id") Long id,
                                   Model model,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        Exam exam = examService.getExamById(id);
        User teacher = userService.getUserById(userDetails.getId());
        if (!exam.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/teacher/exams?accessDenied=true";
        }

        ExamDto dto = new ExamDto();
        dto.setId(exam.getId());
        dto.setTitle(exam.getTitle());
        dto.setDescription(exam.getDescription());
        dto.setSubjectId(exam.getSubject().getId());
        dto.setDuration(exam.getDuration());
        dto.setTotalMarks(exam.getTotalMarks());
        dto.setStartTime(exam.getStartTime());
        dto.setEndTime(exam.getEndTime());
        dto.setStatus(exam.getStatus());

        model.addAttribute("examDto", dto);
        model.addAttribute("exam", exam);
        model.addAttribute("subjects", subjectService.getAllSubjects());
        model.addAttribute("currentUser", userDetails);
        return "teacher/edit-exam";
    }

    @PostMapping("/exams/{id}/edit")
    public String updateExam(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("examDto") ExamDto examDto,
                             BindingResult result,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("subjects", subjectService.getAllSubjects());
            model.addAttribute("currentUser", userDetails);
            return "teacher/edit-exam";
        }
        User teacher = userService.getUserById(userDetails.getId());
        examService.updateExam(id, examDto, teacher, false);
        redirectAttributes.addFlashAttribute("successMessage", "Examination updated successfully!");
        return "redirect:/teacher/exams";
    }

    @PostMapping("/exams/{id}/delete")
    public String deleteExam(@PathVariable("id") Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User teacher = userService.getUserById(userDetails.getId());
            examService.deleteExam(id, teacher, false);
            redirectAttributes.addFlashAttribute("successMessage", "Examination deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/exams";
    }

    // Manage Questions for an Exam
    @GetMapping("/exams/{examId}/questions")
    public String manageQuestions(@PathVariable("examId") Long examId,
                                  Model model,
                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        Exam exam = examService.getExamById(examId);
        User teacher = userService.getUserById(userDetails.getId());
        if (!exam.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/teacher/exams?accessDenied=true";
        }

        List<Question> questions = questionService.getQuestionsByExam(exam);
        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        if (!model.containsAttribute("questionDto")) {
            QuestionDto qDto = new QuestionDto();
            qDto.setExamId(examId);
            qDto.setMarks(1);
            model.addAttribute("questionDto", qDto);
        }
        model.addAttribute("currentUser", userDetails);
        return "teacher/questions";
    }

    @PostMapping("/exams/{examId}/questions/add")
    public String addQuestion(@PathVariable("examId") Long examId,
                              @Valid @ModelAttribute("questionDto") QuestionDto questionDto,
                              BindingResult result,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation failed. Please verify question fields.");
            return "redirect:/teacher/exams/" + examId + "/questions";
        }
        try {
            User teacher = userService.getUserById(userDetails.getId());
            questionService.addQuestion(examId, questionDto, teacher, false);
            redirectAttributes.addFlashAttribute("successMessage", "Question added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/exams/" + examId + "/questions";
    }

    @PostMapping("/questions/{id}/edit")
    public String editQuestion(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("questionDto") QuestionDto questionDto,
                               BindingResult result,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        Question q = questionService.getQuestionById(id);
        Long examId = q.getExam().getId();
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation failed for question update.");
            return "redirect:/teacher/exams/" + examId + "/questions";
        }
        try {
            User teacher = userService.getUserById(userDetails.getId());
            questionService.updateQuestion(id, questionDto, teacher, false);
            redirectAttributes.addFlashAttribute("successMessage", "Question updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/exams/" + examId + "/questions";
    }

    @PostMapping("/questions/{id}/delete")
    public String deleteQuestion(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        Question q = questionService.getQuestionById(id);
        Long examId = q.getExam().getId();
        try {
            User teacher = userService.getUserById(userDetails.getId());
            questionService.deleteQuestion(id, teacher, false);
            redirectAttributes.addFlashAttribute("successMessage", "Question deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/exams/" + examId + "/questions";
    }

    // Student Submissions & Results
    @GetMapping("/submissions")
    public String listSubmissions(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User teacher = userService.getUserById(userDetails.getId());
        List<Submission> submissions = submissionService.getSubmissionsForTeacher(teacher.getId());
        model.addAttribute("submissions", submissions);
        model.addAttribute("currentUser", userDetails);
        return "teacher/submissions";
    }

    @GetMapping("/submissions/{id}")
    public String viewSubmissionDetail(@PathVariable("id") Long id,
                                       Model model,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        Submission submission = submissionService.getSubmissionById(id);
        User teacher = userService.getUserById(userDetails.getId());
        if (!submission.getExam().getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/teacher/submissions?accessDenied=true";
        }

        var answers = resultService.getDetailedAnswers(submission);
        model.addAttribute("submission", submission);
        model.addAttribute("answers", answers);
        model.addAttribute("currentUser", userDetails);
        return "teacher/submission-details";
    }

    // Teacher Profile
    @GetMapping("/profile")
    public String viewProfile(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User teacher = userService.getUserById(userDetails.getId());
        model.addAttribute("user", teacher);
        if (!model.containsAttribute("passwordDto")) {
            model.addAttribute("passwordDto", new PasswordChangeDto());
        }
        model.addAttribute("currentUser", userDetails);
        return "teacher/profile";
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
        return "redirect:/teacher/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@Valid @ModelAttribute("passwordDto") PasswordChangeDto passwordDto,
                                 BindingResult result,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password validation error.");
            return "redirect:/teacher/profile";
        }
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password and confirm password do not match.");
            return "redirect:/teacher/profile";
        }
        try {
            userService.changePassword(userDetails.getId(), passwordDto.getCurrentPassword(), passwordDto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/profile";
    }
}
