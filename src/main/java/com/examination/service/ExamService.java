package com.examination.service;

import com.examination.dto.ExamDto;
import com.examination.entity.Exam;
import com.examination.entity.Subject;
import com.examination.entity.User;
import com.examination.exception.ResourceNotFoundException;
import com.examination.exception.UnauthorizedException;
import com.examination.repository.ExamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ExamService {

    private final ExamRepository examRepository;
    private final SubjectService subjectService;

    public ExamService(ExamRepository examRepository, SubjectService subjectService) {
        this.examRepository = examRepository;
        this.subjectService = subjectService;
    }

    @Transactional(readOnly = true)
    public List<Exam> getAllExams() {
        return examRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Exam> getExamsByTeacher(User teacher) {
        return examRepository.findByTeacherOrderByIdDesc(teacher);
    }

    @Transactional(readOnly = true)
    public List<Exam> getAvailableExams() {
        return examRepository.findByStatusOrderByIdDesc("PUBLISHED");
    }

    @Transactional(readOnly = true)
    public Exam getExamById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));
    }

    public Exam createExam(ExamDto dto, User teacher) {
        Subject subject = subjectService.getSubjectById(dto.getSubjectId());

        Exam exam = new Exam();
        exam.setTitle(dto.getTitle().trim());
        exam.setDescription(dto.getDescription());
        exam.setSubject(subject);
        exam.setTeacher(teacher);
        exam.setDuration(dto.getDuration());
        exam.setTotalMarks(dto.getTotalMarks());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setStatus(dto.getStatus() != null ? dto.getStatus() : "PUBLISHED");

        return examRepository.save(exam);
    }

    public Exam updateExam(Long id, ExamDto dto, User currentUser, boolean isAdmin) {
        Exam exam = getExamById(id);

        if (!isAdmin && !exam.getTeacher().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update this examination");
        }

        Subject subject = subjectService.getSubjectById(dto.getSubjectId());

        exam.setTitle(dto.getTitle().trim());
        exam.setDescription(dto.getDescription());
        exam.setSubject(subject);
        exam.setDuration(dto.getDuration());
        exam.setTotalMarks(dto.getTotalMarks());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        if (dto.getStatus() != null) {
            exam.setStatus(dto.getStatus());
        }

        return examRepository.save(exam);
    }

    public void deleteExam(Long id, User currentUser, boolean isAdmin) {
        Exam exam = getExamById(id);

        if (!isAdmin && !exam.getTeacher().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this examination");
        }

        examRepository.delete(exam);
    }

    @Transactional(readOnly = true)
    public List<Exam> searchExams(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllExams();
        }
        return examRepository.searchExams(keyword.trim());
    }

    @Transactional(readOnly = true)
    public long countTotalExams() {
        return examRepository.count();
    }

    @Transactional(readOnly = true)
    public long countTeacherExams(User teacher) {
        return examRepository.countByTeacher(teacher);
    }

    @Transactional(readOnly = true)
    public List<Exam> getRecentExams() {
        return examRepository.findTop5ByOrderByIdDesc();
    }
}
