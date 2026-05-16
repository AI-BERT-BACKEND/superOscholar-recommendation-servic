package com.aibert.dosw.infrastructure.adapters.out.db;

import com.aibert.dosw.domain.model.StudentActivityLog;
import com.aibert.dosw.domain.port.out.StudentActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoStudentActivityLogRepositoryAdapter implements StudentActivityLogRepository {

    private final SpringDataMongoStudentActivityLogRepository repository;

    @Override
    public StudentActivityLog save(StudentActivityLog log) {
        return repository.save(log);
    }

    @Override
    public List<StudentActivityLog> findByStudentId(String studentId) {
        return repository.findByStudentId(studentId);
    }

    @Override
    public List<StudentActivityLog> findByStudentIdAndActivityType(String studentId, String activityType) {
        return repository.findByStudentIdAndActivityType(studentId, activityType);
    }

    @Override
    public Optional<StudentActivityLog> findFirstByStudentIdOrderByLogDateAsc(String studentId) {
        return repository.findFirstByStudentIdOrderByLogDateAsc(studentId);
    }
}

interface SpringDataMongoStudentActivityLogRepository extends MongoRepository<StudentActivityLog, String> {
    List<StudentActivityLog> findByStudentId(String studentId);

    List<StudentActivityLog> findByStudentIdAndActivityType(String studentId, String activityType);

    Optional<StudentActivityLog> findFirstByStudentIdOrderByLogDateAsc(String studentId);
}
