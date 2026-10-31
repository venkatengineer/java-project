package com.examination.service;

import com.examination.entity.Exam;
import com.examination.entity.StudentAnswer;
import com.examination.entity.Submission;
import com.examination.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ResultService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionService submissionService;

    public ResultService(SubmissionRepository submissionRepository, SubmissionService submissionService) {
        this.submissionRepository = submissionRepository;
        this.submissionService = submissionService;
    }

    public Submission getSubmissionResult(Long submissionId) {
        return submissionService.getSubmissionById(submissionId);
    }

    public List<StudentAnswer> getDetailedAnswers(Submission submission) {
        return submissionService.getAnswersBySubmission(submission);
    }

    public Map<String, Object> getExamStatistics(Exam exam) {
        List<Submission> submissions = submissionRepository.findByExamOrderBySubmittedAtDesc(exam);
        Map<String, Object> stats = new HashMap<>();

        int totalAttempts = submissions.size();
        stats.put("totalAttempts", totalAttempts);

        if (totalAttempts == 0) {
            stats.put("averageScore", 0.0);
            stats.put("highestScore", 0);
            stats.put("lowestScore", 0);
            stats.put("passRate", 0.0);
            return stats;
        }

        int maxScore = Integer.MIN_VALUE;
        int minScore = Integer.MAX_VALUE;
        int sumScore = 0;
        int passCount = 0;

        for (Submission s : submissions) {
            int score = s.getScore() != null ? s.getScore() : 0;
            sumScore += score;
            if (score > maxScore) maxScore = score;
            if (score < minScore) minScore = score;
            if (s.isPassed()) passCount++;
        }

        double avgScore = Math.round(((double) sumScore / totalAttempts) * 10.0) / 10.0;
        double passRate = Math.round(((double) passCount / totalAttempts * 100.0) * 10.0) / 10.0;

        stats.put("averageScore", avgScore);
        stats.put("highestScore", maxScore);
        stats.put("lowestScore", minScore);
        stats.put("passRate", passRate);

        return stats;
    }
}
