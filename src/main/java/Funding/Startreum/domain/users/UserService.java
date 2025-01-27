package Funding.Startreum.domain.users;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // 허용된 역할 목록 (처음에는 ADMIN 포함)
    private final Set<User.Role> allowedRoles = Set.of(User.Role.BENEFICIARY, User.Role.SPONSOR, User.Role.ADMIN);

    // ADMIN 역할을 제거한 허용된 역할 목록
    //private final Set<User.Role> allowedRoles = Set.of(User.Role.BENEFICIARY, User.Role.SPONSOR);

    // 회원가입
    public void registerUser(SignupRequest signupRequest) {

        // 입력 값 검증
        validateSignupRequest(signupRequest);


        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(signupRequest.password());

        // 사용자 엔티티 생성
        User user = new User();
        user.setName(signupRequest.name());
        user.setEmail(signupRequest.email());
        user.setPassword(encryptedPassword);
        user.setRole(signupRequest.role());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 데이터베이스에 저장
        userRepository.save(user);
    }

    // 입력 값 검증 로직
    private void validateSignupRequest(SignupRequest signupRequest) {
        // 이메일 중복 확인
        if (isEmailDuplicate(signupRequest.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 이름(ID) 중복 확인
        if (isNameDuplicate(signupRequest.name())) {
            throw new IllegalArgumentException("이미 사용 중인 이름(ID)입니다.");
        }

        // 역할 검증
        if (!allowedRoles.contains(signupRequest.role())) {
            throw new IllegalArgumentException("허용되지 않은 역할(Role)입니다.");
        }
    }

    // 이름(ID) 중복 확인
    public boolean isNameDuplicate(String name) {
        return userRepository.findByName(name).isPresent();
    }

    // 이메일 중복 확인
    public boolean isEmailDuplicate(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}