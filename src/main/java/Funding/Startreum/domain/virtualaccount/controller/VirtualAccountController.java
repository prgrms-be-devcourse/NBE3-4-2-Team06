package Funding.Startreum.domain.virtualaccount.controller;

import Funding.Startreum.domain.virtualaccount.service.VirtualAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VirtualAccountController {

    private final VirtualAccountService service;

}
