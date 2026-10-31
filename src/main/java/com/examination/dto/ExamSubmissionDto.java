package com.examination.dto;

import java.util.HashMap;
import java.util.Map;

public class ExamSubmissionDto {

    private Long examId;
    // Map of questionId -> selectedAnswer ("A", "B", "C", "D" or empty)
    private Map<Long, String> answers = new HashMap<>();

    public ExamSubmissionDto() {
    }

    public ExamSubmissionDto(Long examId) {
        this.examId = examId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Map<Long, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Long, String> answers) {
        this.answers = answers;
    }
}
