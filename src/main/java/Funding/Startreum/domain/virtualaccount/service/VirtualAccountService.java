package Funding.Startreum.domain.virtualaccount.service;

import Funding.Startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VirtualAccountService {

    private final VirtualAccountRepository repository;

}
