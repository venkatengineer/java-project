package com.examination.service;

import com.examination.dto.ExamSubmissionDto;
import com.examination.entity.Exam;
import com.examination.entity.Question;
import com.examination.entity.StudentAnswer;
import com.examination.entity.Submission;
import com.examination.entity.User;
import com.examination.exception.ExamException;
import com.examination.exception.ResourceNotFoundException;
import com.examination.repository.ExamRepository;
import com.examination.repository.QuestionRepository;
import com.examination.repository.StudentAnswerRepository;
import com.examination.repository.SubmissionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
                             StudentAnswerRepository studentAnswerRepository,
                             ExamRepository examRepository,
                             QuestionRepository questionRepository) {
        this.submissionRepository = submissionRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public Submission submitExam(Long examId, User student, ExamSubmissionDto submissionDto) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + examId));

        if (!"PUBLISHED".equalsIgnoreCase(exam.getStatus())) {
            throw new ExamException("This examination is not open for submissions");
        }

        // Prevent duplicate submissions
        if (submissionRepository.existsByStudentAndExam(student, exam)) {
            throw new ExamException("You have already submitted this examination. Duplicate attempts are not allowed.");
        }

        List<Question> questions = questionRepository.findByExamOrderByIdAsc(exam);
        Map<Long, String> answersMap = submissionDto != null && submissionDto.getAnswers() != null
                ? submissionDto.getAnswers()
                : Map.of();

        int totalScore = 0;
        Submission submission = new Submission();
        submission.setStudent(student);
        submission.setExam(exam);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus("COMPLETED");

        Submission savedSubmission = submissionRepository.save(submission);

        List<StudentAnswer> studentAnswers = new ArrayList<>();
        for (Question q : questions) {
            String selectedOption = answersMap.get(q.getId());
            if (selectedOption != null) {
                selectedOption = selectedOption.trim().toUpperCase();
            }

            StudentAnswer studentAnswer = new StudentAnswer(savedSubmission, q, selectedOption);
            studentAnswers.add(studentAnswer);

            if (selectedOption != null && selectedOption.equalsIgnoreCase(q.getCorrectAnswer().trim())) {
                totalScore += q.getMarks();
            }
        }

        studentAnswerRepository.saveAll(studentAnswers);
        savedSubmission.setScore(totalScore);
        savedSubmission.setStudentAnswers(studentAnswers);

        return submissionRepository.save(savedSubmission);
    }

    @Transactional(readOnly = true)
    public boolean hasStudentAttempted(User student, Exam exam) {
        return submissionRepository.existsByStudentAndExam(student, exam);
    }

    @Transactional(readOnly = true)
    public Optional<Submission> getSubmissionForStudentAndExam(User student, Exam exam) {
        return submissionRepository.findByStudentAndExam(student, exam);
    }

    @Transactional(readOnly = true)
    public Submission getSubmissionById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByStudent(User student) {
        return submissionRepository.findByStudentOrderBySubmittedAtDesc(student);
    }

    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByExam(Exam exam) {
        return submissionRepository.findByExamOrderBySubmittedAtDesc(exam);
    }

    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsForTeacher(Long teacherId) {
        return submissionRepository.findAllByTeacherId(teacherId);
    }

    @Transactional(readOnly = true)
    public List<Submission> getRecentTeacherSubmissions(Long teacherId, int limit) {
        return submissionRepository.findRecentByTeacherId(teacherId, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAllByOrderBySubmittedAtDesc();
    }

    @Transactional(readOnly = true)
    public long countStudentSubmissions(User student) {
        return submissionRepository.countByStudent(student);
    }

    @Transactional(readOnly = true)
    public long countSubmissionsByTeacher(Long teacherId) {
        return submissionRepository.countByTeacherId(teacherId);
    }

    @Transactional(readOnly = true)
    public double getStudentAveragePercentage(Long studentId) {
        Double avg = submissionRepository.findAveragePercentageByStudentId(studentId);
        if (avg == null) {
            return 0.0;
        }
        return Math.round(avg * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public List<StudentAnswer> getAnswersBySubmission(Submission submission) {
        return studentAnswerRepository.findBySubmissionOrderByIdAsc(submission);
    }
}
