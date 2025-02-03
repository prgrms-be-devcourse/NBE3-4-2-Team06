package Funding.Startreum.domain.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProjectUpdateResponseDto (
        Integer projectId,
        String title,
        String description,
        BigDecimal fundingGoal,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime updatedAt
){}
