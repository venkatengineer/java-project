package com.examination.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_answers", indexes = {
    @Index(name = "idx_answer_submission", columnList = "submission_id"),
    @Index(name = "idx_answer_question", columnList = "question_id")
})
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "selected_answer", length = 5)
    private String selectedAnswer; // "A", "B", "C", "D" or null

    public StudentAnswer() {
    }

    public StudentAnswer(Submission submission, Question question, String selectedAnswer) {
        this.submission = submission;
        this.question = question;
        this.selectedAnswer = selectedAnswer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }

    public boolean isCorrect() {
        return question != null &&
               selectedAnswer != null &&
               selectedAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer() != null ? question.getCorrectAnswer().trim() : "");
    }
}
