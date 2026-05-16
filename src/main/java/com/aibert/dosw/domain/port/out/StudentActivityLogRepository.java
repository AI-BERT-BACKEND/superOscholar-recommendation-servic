package com.aibert.dosw.domain.port.out;

import com.aibert.dosw.domain.model.StudentActivityLog;
import java.util.List;

import java.util.Optional;

public interface StudentActivityLogRepository {
    StudentActivityLog save(StudentActivityLog log);

    List<StudentActivityLog> findByStudentId(String studentId);

    List<StudentActivityLog> findByStudentIdAndActivityType(String studentId, String activityType);

    Optional<StudentActivityLog> findFirstByStudentIdOrderByLogDateAsc(String studentId);
}
