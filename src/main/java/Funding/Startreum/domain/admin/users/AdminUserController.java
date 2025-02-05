package Funding.Startreum.domain.admin.users;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserList() {
        UserListResponse response = adminUserService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserDetail(@PathVariable Integer userId) {
        UserDetailResponse response = adminUserService.getUserDetail(userId);
        return ResponseEntity.ok(response);
    }
}