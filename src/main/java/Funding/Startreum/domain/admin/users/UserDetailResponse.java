package Funding.Startreum.domain.admin.users;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserDetailResponse {
    private String status;
    private int statusCode;
    private String message;
    private UserDTO data;

    @Getter
    @Setter
    public static class UserDTO {
        private Integer userId;
        private String name;
        private String email;
        private String role;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}