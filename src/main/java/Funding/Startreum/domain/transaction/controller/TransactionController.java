package Funding.Startreum.domain.transaction.controller;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/projects")
public class TransactionController {

    private final TransactionService service;

}
