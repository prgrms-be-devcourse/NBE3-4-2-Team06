package Funding.Startreum.domain.admin.users;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UserListResponseDTO {
    private String status;
    private int statusCode;
    private String message;
    private List<UserDTO> data;

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
