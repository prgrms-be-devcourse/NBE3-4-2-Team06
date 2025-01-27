package Funding.Startreum.domain.users;

import java.time.LocalDateTime;

// 응답용 DTO
public record UserResponse(
        Integer userId,
        String name,
        String email,
        User.Role role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}