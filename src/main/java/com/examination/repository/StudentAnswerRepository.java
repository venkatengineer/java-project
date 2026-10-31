package com.examination.repository;

import com.examination.entity.StudentAnswer;
import com.examination.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    List<StudentAnswer> findBySubmissionOrderByIdAsc(Submission submission);

    void deleteBySubmission(Submission submission);
}
