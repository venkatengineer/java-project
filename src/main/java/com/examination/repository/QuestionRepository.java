package com.examination.repository;

import com.examination.entity.Exam;
import com.examination.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByExamOrderByIdAsc(Exam exam);

    long countByExam(Exam exam);

    void deleteByExam(Exam exam);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.exam.teacher.id = :teacherId")
    long countByTeacherId(@Param("teacherId") Long teacherId);
}
