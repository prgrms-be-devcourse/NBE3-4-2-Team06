package Funding.Startreum.domain.admin.users;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController  {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String getUserList(Model model) {
        model.addAttribute("users", adminUserService.getAllUsers().getData());
        return "admin/users/list";
    }
}