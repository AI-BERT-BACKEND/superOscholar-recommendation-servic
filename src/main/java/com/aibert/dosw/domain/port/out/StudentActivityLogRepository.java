package com.aibert.dosw.domain.port.out;

import com.aibert.dosw.domain.model.StudentActivityLog;
import java.util.List;

import java.util.Optional;

public interface StudentActivityLogRepository {
    StudentActivityLog save(StudentActivityLog log);
    List<StudentActivityLog> findByStudentId(Long studentId);
    List<StudentActivityLog> findByStudentIdAndActivityType(Long studentId, String activityType);
    Optional<StudentActivityLog> findFirstByStudentIdOrderByLogDateAsc(Long studentId);
}
