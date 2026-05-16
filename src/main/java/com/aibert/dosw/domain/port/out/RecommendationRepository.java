package com.aibert.dosw.domain.port.out;

import com.aibert.dosw.domain.model.Recommendation;
import java.time.LocalDate;
import java.util.Optional;

public interface RecommendationRepository {
    Recommendation save(Recommendation recommendation);

    Optional<Recommendation> findByStudentIdAndDateGenerated(String studentId, LocalDate dateGenerated);
}
