package com.examination.controller;

import com.examination.dto.ExamDto;
import com.examination.dto.PasswordChangeDto;
import com.examination.dto.UserDto;
import com.examination.entity.Exam;
import com.examination.entity.Role;
import com.examination.entity.Subject;
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
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final SubjectService subjectService;
    private final ExamService examService;
    private final SubmissionService submissionService;
    private final ResultService resultService;

    public AdminController(UserService userService,
                           SubjectService subjectService,
                           ExamService examService,
                           SubmissionService submissionService,
                           ResultService resultService) {
        this.userService = userService;
        this.subjectService = subjectService;
        this.examService = examService;
        this.submissionService = submissionService;
        this.resultService = resultService;
    }

    // Admin Dashboard
    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("totalStudents", userService.countByRole(Role.STUDENT));
        model.addAttribute("totalTeachers", userService.countByRole(Role.TEACHER));
        model.addAttribute("totalExams", examService.countTotalExams());
        model.addAttribute("totalSubjects", subjectService.countSubjects());
        model.addAttribute("recentExams", examService.getRecentExams());
        model.addAttribute("recentUsers", userService.getRecentUsers());
        model.addAttribute("currentUser", userDetails);
        return "admin/dashboard";
    }

    // --- MANAGE STUDENTS ---
    @GetMapping("/students")
    public String listStudents(@RequestParam(value = "search", required = false) String search,
                               Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<User> students = (search != null && !search.trim().isEmpty())
                ? userService.searchUsers(Role.STUDENT, search.trim())
                : userService.getUsersByRole(Role.STUDENT);

        model.addAttribute("students", students);
        model.addAttribute("searchQuery", search);
        if (!model.containsAttribute("userDto")) {
            model.addAttribute("userDto", new UserDto());
        }
        model.addAttribute("currentUser", userDetails);
        return "admin/students";
    }

    @PostMapping("/students/add")
    public String addStudent(@Valid @ModelAttribute("userDto") UserDto userDto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userDto", result);
            redirectAttributes.addFlashAttribute("userDto", userDto);
            redirectAttributes.addFlashAttribute("errorMessage", "Validation failed. Please check your inputs.");
            return "redirect:/admin/students";
        }
        try {
            userService.createUser(userDto, Role.STUDENT);
            redirectAttributes.addFlashAttribute("successMessage", "Student '" + userDto.getName() + "' created successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @PostMapping("/students/edit/{id}")
    public String editStudent(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("userDto") UserDto userDto,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation error occurred while updating student.");
            return "redirect:/admin/students";
        }
        try {
            userService.updateUser(id, userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Student updated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @PostMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete student: " + e.getMessage());
        }
        return "redirect:/admin/students";
    }

    // --- MANAGE TEACHERS ---
    @GetMapping("/teachers")
    public String listTeachers(@RequestParam(value = "search", required = false) String search,
                               Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<User> teachers = (search != null && !search.trim().isEmpty())
                ? userService.searchUsers(Role.TEACHER, search.trim())
                : userService.getUsersByRole(Role.TEACHER);

        model.addAttribute("teachers", teachers);
        model.addAttribute("searchQuery", search);
        if (!model.containsAttribute("userDto")) {
            model.addAttribute("userDto", new UserDto());
        }
        model.addAttribute("currentUser", userDetails);
        return "admin/teachers";
    }

    @PostMapping("/teachers/add")
    public String addTeacher(@Valid @ModelAttribute("userDto") UserDto userDto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation failed. Please verify teacher details.");
            return "redirect:/admin/teachers";
        }
        try {
            userService.createUser(userDto, Role.TEACHER);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher '" + userDto.getName() + "' created successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/teachers";
    }

    @PostMapping("/teachers/edit/{id}")
    public String editTeacher(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("userDto") UserDto userDto,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation failed while editing teacher.");
            return "redirect:/admin/teachers";
        }
        try {
            userService.updateUser(id, userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher updated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/teachers";
    }

    @PostMapping("/teachers/delete/{id}")
    public String deleteTeacher(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete teacher: " + e.getMessage());
        }
        return "redirect:/admin/teachers";
    }

    // --- MANAGE SUBJECTS ---
    @GetMapping("/subjects")
    public String listSubjects(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("subjects", subjectService.getAllSubjects());
        if (!model.containsAttribute("newSubject")) {
            model.addAttribute("newSubject", new Subject());
        }
        model.addAttribute("currentUser", userDetails);
        return "admin/subjects";
    }

    @PostMapping("/subjects/add")
    public String addSubject(@ModelAttribute("newSubject") Subject subject,
                             RedirectAttributes redirectAttributes) {
        if (subject.getSubjectName() == null || subject.getSubjectName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Subject name cannot be empty.");
            return "redirect:/admin/subjects";
        }
        try {
            subjectService.createSubject(subject);
            redirectAttributes.addFlashAttribute("successMessage", "Subject '" + subject.getSubjectName() + "' created successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/subjects";
    }

    @PostMapping("/subjects/edit/{id}")
    public String editSubject(@PathVariable("id") Long id,
                              @ModelAttribute("subject") Subject subject,
                              RedirectAttributes redirectAttributes) {
        try {
            subjectService.updateSubject(id, subject);
            redirectAttributes.addFlashAttribute("successMessage", "Subject updated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/subjects";
    }

    @PostMapping("/subjects/delete/{id}")
    public String deleteSubject(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            subjectService.deleteSubject(id);
            redirectAttributes.addFlashAttribute("successMessage", "Subject deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete subject: " + e.getMessage());
        }
        return "redirect:/admin/subjects";
    }

    // --- MANAGE EXAMINATIONS ---
    @GetMapping("/exams")
    public String listExams(@RequestParam(value = "search", required = false) String search,
                            Model model,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Exam> exams = (search != null && !search.trim().isEmpty())
                ? examService.searchExams(search)
                : examService.getAllExams();

        model.addAttribute("exams", exams);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentUser", userDetails);
        return "admin/exams";
    }

    @PostMapping("/exams/delete/{id}")
    public String deleteExam(@PathVariable("id") Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(userDetails.getId());
            examService.deleteExam(id, user, true);
            redirectAttributes.addFlashAttribute("successMessage", "Examination deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete examination: " + e.getMessage());
        }
        return "redirect:/admin/exams";
    }

    // --- EXAMINATION RESULTS ---
    @GetMapping("/results")
    public String listResults(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("submissions", submissionService.getAllSubmissions());
        model.addAttribute("currentUser", userDetails);
        return "admin/results";
    }

    @GetMapping("/results/{id}")
    public String viewResultDetails(@PathVariable("id") Long id,
                                    Model model,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        var submission = submissionService.getSubmissionById(id);
        var answers = resultService.getDetailedAnswers(submission);
        model.addAttribute("submission", submission);
        model.addAttribute("answers", answers);
        model.addAttribute("currentUser", userDetails);
        return "admin/result-details";
    }

    // --- ADMIN PROFILE ---
    @GetMapping("/profile")
    public String viewProfile(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userService.getUserById(userDetails.getId());
        model.addAttribute("user", user);
        if (!model.containsAttribute("passwordDto")) {
            model.addAttribute("passwordDto", new PasswordChangeDto());
        }
        model.addAttribute("currentUser", userDetails);
        return "admin/profile";
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
        return "redirect:/admin/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@Valid @ModelAttribute("passwordDto") PasswordChangeDto passwordDto,
                                 BindingResult result,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password validation error.");
            return "redirect:/admin/profile";
        }
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password and confirmation password do not match.");
            return "redirect:/admin/profile";
        }
        try {
            userService.changePassword(userDetails.getId(), passwordDto.getCurrentPassword(), passwordDto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/profile";
    }
}
