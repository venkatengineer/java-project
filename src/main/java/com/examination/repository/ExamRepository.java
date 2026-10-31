package com.examination.repository;

import com.examination.entity.Exam;
import com.examination.entity.Subject;
import com.examination.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByTeacherOrderByIdDesc(User teacher);

    List<Exam> findByStatusOrderByIdDesc(String status);

    List<Exam> findAllByOrderByIdDesc();

    List<Exam> findBySubjectOrderByIdDesc(Subject subject);

    long countByTeacher(User teacher);

    long countByStatus(String status);

    List<Exam> findTop5ByOrderByIdDesc();

    @Query("SELECT e FROM Exam e WHERE " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.subject.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY e.id DESC")
    List<Exam> searchExams(@Param("keyword") String keyword);
}
