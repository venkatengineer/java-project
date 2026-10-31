package com.examination.repository;

import com.examination.entity.Exam;
import com.examination.entity.Submission;
import com.examination.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByStudentOrderBySubmittedAtDesc(User student);

    List<Submission> findByExamOrderBySubmittedAtDesc(Exam exam);

    Optional<Submission> findByStudentAndExam(User student, Exam exam);

    boolean existsByStudentAndExam(User student, Exam exam);

    long countByStudent(User student);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.exam.teacher.id = :teacherId")
    long countByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT s FROM Submission s WHERE s.exam.teacher.id = :teacherId ORDER BY s.submittedAt DESC")
    List<Submission> findRecentByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);

    @Query("SELECT s FROM Submission s WHERE s.exam.teacher.id = :teacherId ORDER BY s.submittedAt DESC")
    List<Submission> findAllByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT AVG(s.score * 100.0 / s.exam.totalMarks) FROM Submission s WHERE s.student.id = :studentId AND s.exam.totalMarks > 0")
    Double findAveragePercentageByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT AVG(s.score) FROM Submission s")
    Double findOverallAverageScore();

    List<Submission> findTop10ByOrderBySubmittedAtDesc();

    List<Submission> findAllByOrderBySubmittedAtDesc();
}
