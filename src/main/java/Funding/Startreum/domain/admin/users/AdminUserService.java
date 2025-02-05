package Funding.Startreum.domain.admin.users;

import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional
    public UserListResponseDTO getAllUsers() {
        List<User> users = userRepository.findAll();

        UserListResponseDTO response = new UserListResponseDTO();
        response.setStatus("ok");
        response.setStatusCode(200);
        response.setMessage("회원 목록 조회에 성공했습니다.");

        List<UserListResponseDTO.UserDTO> userDTOs = users.stream()
                .map(user -> {
                    UserListResponseDTO.UserDTO dto = new UserListResponseDTO.UserDTO();
                    dto.setUserId(user.getUserId());
                    dto.setName(user.getName());
                    dto.setEmail(user.getEmail());
                    dto.setRole(user.getRole().name());
                    dto.setCreatedAt(user.getCreatedAt());
                    dto.setUpdatedAt(user.getUpdatedAt());
                    return dto;
                })
                .collect(Collectors.toList());

        response.setData(userDTOs);
        return response;
    }
}
