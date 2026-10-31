package com.examination.service;

import com.examination.dto.QuestionDto;
import com.examination.dto.StudentExamQuestionDto;
import com.examination.entity.Exam;
import com.examination.entity.Question;
import com.examination.entity.User;
import com.examination.exception.ResourceNotFoundException;
import com.examination.exception.UnauthorizedException;
import com.examination.repository.ExamRepository;
import com.examination.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;

    public QuestionService(QuestionRepository questionRepository, ExamRepository examRepository) {
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
    }

    @Transactional(readOnly = true)
    public List<Question> getQuestionsByExam(Exam exam) {
        return questionRepository.findByExamOrderByIdAsc(exam);
    }

    /**
     * Returns exam questions without revealing correct answers (secure for students).
     */
    @Transactional(readOnly = true)
    public List<StudentExamQuestionDto> getStudentQuestions(Exam exam) {
        List<Question> questions = questionRepository.findByExamOrderByIdAsc(exam);
        List<StudentExamQuestionDto> studentQuestions = new ArrayList<>();

        int count = 1;
        for (Question q : questions) {
            StudentExamQuestionDto dto = new StudentExamQuestionDto(
                    q.getId(),
                    count++,
                    q.getQuestionText(),
                    q.getOptionA(),
                    q.getOptionB(),
                    q.getOptionC(),
                    q.getOptionD(),
                    q.getMarks()
            );
            studentQuestions.add(dto);
        }
        return studentQuestions;
    }

    @Transactional(readOnly = true)
    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
    }

    public Question addQuestion(Long examId, QuestionDto dto, User currentUser, boolean isAdmin) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        if (!isAdmin && !exam.getTeacher().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to add questions to this examination");
        }

        Question question = new Question();
        question.setExam(exam);
        question.setQuestionText(dto.getQuestionText().trim());
        question.setOptionA(dto.getOptionA().trim());
        question.setOptionB(dto.getOptionB().trim());
        question.setOptionC(dto.getOptionC().trim());
        question.setOptionD(dto.getOptionD().trim());
        question.setCorrectAnswer(dto.getCorrectAnswer().trim().toUpperCase());
        question.setMarks(dto.getMarks() != null ? dto.getMarks() : 1);

        Question saved = questionRepository.save(question);
        updateExamTotalMarks(exam);
        return saved;
    }

    public Question updateQuestion(Long id, QuestionDto dto, User currentUser, boolean isAdmin) {
        Question question = getQuestionById(id);
        Exam exam = question.getExam();

        if (!isAdmin && !exam.getTeacher().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update this question");
        }

        question.setQuestionText(dto.getQuestionText().trim());
        question.setOptionA(dto.getOptionA().trim());
        question.setOptionB(dto.getOptionB().trim());
        question.setOptionC(dto.getOptionC().trim());
        question.setOptionD(dto.getOptionD().trim());
        question.setCorrectAnswer(dto.getCorrectAnswer().trim().toUpperCase());
        question.setMarks(dto.getMarks() != null ? dto.getMarks() : 1);

        Question saved = questionRepository.save(question);
        updateExamTotalMarks(exam);
        return saved;
    }

    public void deleteQuestion(Long id, User currentUser, boolean isAdmin) {
        Question question = getQuestionById(id);
        Exam exam = question.getExam();

        if (!isAdmin && !exam.getTeacher().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this question");
        }

        questionRepository.delete(question);
        updateExamTotalMarks(exam);
    }

    private void updateExamTotalMarks(Exam exam) {
        List<Question> questions = questionRepository.findByExamOrderByIdAsc(exam);
        int sumMarks = questions.stream().mapToInt(Question::getMarks).sum();
        if (sumMarks > 0) {
            exam.setTotalMarks(sumMarks);
            examRepository.save(exam);
        }
    }

    @Transactional(readOnly = true)
    public long countQuestionsByExam(Exam exam) {
        return questionRepository.countByExam(exam);
    }

    @Transactional(readOnly = true)
    public long countQuestionsByTeacher(Long teacherId) {
        return questionRepository.countByTeacherId(teacherId);
    }
}
