package com.aibert.dosw.infrastructure.adapters.out.db;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.domain.port.out.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoRecommendationRepositoryAdapter implements RecommendationRepository {

    private final SpringDataMongoRecommendationRepository repository;

    @Override
    public Recommendation save(Recommendation recommendation) {
        return repository.save(recommendation);
    }

    @Override
    public Optional<Recommendation> findByStudentIdAndDateGenerated(String studentId, LocalDate dateGenerated) {
        return repository.findByStudentIdAndDateGenerated(studentId, dateGenerated);
    }
}

interface SpringDataMongoRecommendationRepository extends MongoRepository<Recommendation, String> {
    Optional<Recommendation> findByStudentIdAndDateGenerated(String studentId, LocalDate dateGenerated);
}
