package com.aibert.dosw.domain.port.out;

import com.aibert.dosw.domain.model.TaskDTO;
import java.util.List;

public interface TaskServicePort {
    List<TaskDTO> getPrioritizedTasks(Long studentId);
}
