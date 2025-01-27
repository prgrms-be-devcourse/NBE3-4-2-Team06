package Funding.Startreum.domain.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email); // 이메일 중복 확인, 검색
    Optional<User> findByName(String name);   // 이름 중복 확인, 검색
}