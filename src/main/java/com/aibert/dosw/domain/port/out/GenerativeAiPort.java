package com.aibert.dosw.domain.port.out;

import com.aibert.dosw.domain.model.Recommendation;

public interface GenerativeAiPort {
    Recommendation generateRecommendation(Long studentId, String enrichedContext);
}
