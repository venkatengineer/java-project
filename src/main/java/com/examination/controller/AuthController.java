package com.examination.controller;

import com.examination.dto.UserDto;
import com.examination.entity.Role;
import com.examination.service.CustomUserDetails;
import com.examination.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/", "/index"})
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            Role role = userDetails.getRole();
            if (role == Role.ADMIN) return "redirect:/admin/dashboard";
            if (role == Role.TEACHER) return "redirect:/teacher/dashboard";
            if (role == Role.STUDENT) return "redirect:/student/dashboard";
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/login/student";
    }

    @GetMapping("/login/admin")
    public String adminLogin(@RequestParam(value = "error", required = false) String error,
                             @RequestParam(value = "logout", required = false) String logout,
                             Model model) {
        model.addAttribute("roleTitle", "Administrator");
        model.addAttribute("loginRole", "admin");
        model.addAttribute("portalBadge", "Admin Portal");
        model.addAttribute("sampleEmail", "admin@exam.com");
        model.addAttribute("samplePassword", "admin123");
        if (error != null) model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        if (logout != null) model.addAttribute("successMessage", "You have been logged out successfully.");
        return "auth/admin-login";
    }

    @GetMapping("/login/teacher")
    public String teacherLogin(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        model.addAttribute("roleTitle", "Faculty & Teacher");
        model.addAttribute("loginRole", "teacher");
        model.addAttribute("portalBadge", "Teacher Portal");
        model.addAttribute("sampleEmail", "sarah.teacher@exam.com");
        model.addAttribute("samplePassword", "teacher123");
        if (error != null) model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        if (logout != null) model.addAttribute("successMessage", "You have been logged out successfully.");
        return "auth/teacher-login";
    }

    @GetMapping("/login/student")
    public String studentLogin(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        model.addAttribute("roleTitle", "Student Portal");
        model.addAttribute("loginRole", "student");
        model.addAttribute("portalBadge", "Student Access");
        model.addAttribute("sampleEmail", "alice.student@exam.com");
        model.addAttribute("samplePassword", "student123");
        if (error != null) model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        if (logout != null) model.addAttribute("successMessage", "You have been logged out successfully.");
        return "auth/student-login";
    }

    @GetMapping("/register/student")
    public String showStudentRegistration(Model model) {
        if (!model.containsAttribute("userDto")) {
            model.addAttribute("userDto", new UserDto());
        }
        return "auth/student-register";
    }

    @PostMapping("/register/student")
    public String registerStudent(@Valid @ModelAttribute("userDto") UserDto userDto,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/student-register";
        }
        try {
            userService.createUser(userDto, Role.STUDENT);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You can now log in.");
            return "redirect:/login/student";
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "error.userDto", e.getMessage());
            return "auth/student-register";
        }
    }
}
