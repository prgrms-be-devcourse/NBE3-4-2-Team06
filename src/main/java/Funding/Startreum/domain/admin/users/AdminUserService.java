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
    public UserListResponse getAllUsers() {
        List<User> users = userRepository.findAll();

        UserListResponse response = new UserListResponse();
        response.setStatus("ok");
        response.setStatusCode(200);
        response.setMessage("회원 목록 조회에 성공했습니다.");

        List<UserListResponse.UserDTO> userDTOs = users.stream()
                .map(user -> {
                    UserListResponse.UserDTO dto = new UserListResponse.UserDTO();
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

    @Transactional
    public UserDetailResponse getUserDetail(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        UserDetailResponse response = new UserDetailResponse();
        response.setStatus("ok");
        response.setStatusCode(200);
        response.setMessage("회원 조회에 성공했습니다.");

        UserDetailResponse.UserDTO userDTO = new UserDetailResponse.UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole().name());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());

        response.setData(userDTO);
        return response;
    }
}
