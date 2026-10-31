package com.examination.service;

import com.examination.entity.Subject;
import com.examination.exception.ResourceNotFoundException;
import com.examination.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Transactional(readOnly = true)
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAllByOrderBySubjectNameAsc();
    }

    @Transactional(readOnly = true)
    public Subject getSubjectById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
    }

    public Subject createSubject(Subject subject) {
        if (subjectRepository.existsBySubjectName(subject.getSubjectName().trim())) {
            throw new IllegalArgumentException("Subject '" + subject.getSubjectName() + "' already exists");
        }
        subject.setSubjectName(subject.getSubjectName().trim());
        return subjectRepository.save(subject);
    }

    public Subject updateSubject(Long id, Subject updated) {
        Subject subject = getSubjectById(id);
        if (subjectRepository.existsBySubjectNameAndIdNot(updated.getSubjectName().trim(), id)) {
            throw new IllegalArgumentException("Subject '" + updated.getSubjectName() + "' already exists");
        }
        subject.setSubjectName(updated.getSubjectName().trim());
        subject.setDescription(updated.getDescription());
        return subjectRepository.save(subject);
    }

    public void deleteSubject(Long id) {
        Subject subject = getSubjectById(id);
        subjectRepository.delete(subject);
    }

    @Transactional(readOnly = true)
    public long countSubjects() {
        return subjectRepository.count();
    }
}
