package Funding.Startreum.domain.reward.controller;

import Funding.Startreum.domain.reward.dto.request.RewardRequest;
import Funding.Startreum.domain.reward.dto.request.RewardUpdateRequest;
import Funding.Startreum.domain.reward.dto.response.RewardResponse;
import Funding.Startreum.domain.reward.service.RewardService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reward")
public class RewardRestController {

    private final RewardService service;

    @PreAuthorize("hasAnyRole('ADMIN', 'BENEFICIARY')")
    @PostMapping
    public ResponseEntity<?> createReward(
            @RequestBody @Valid RewardRequest request
    ) {

        RewardResponse rewardResponse = service.createReward(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        Map.of(
                                "status", "success",
                                "message", "리워드 생성에 성공했습니다.",
                                "data", rewardResponse
                        )
                );
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getRewardByProjectId(
            @PathVariable(name = "projectId") int projectId
    ) {
        List<RewardResponse> rewardResponse = service.getRewardsByProjectId(projectId);

        if (rewardResponse.isEmpty()) {
            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "리워드가 존재하지 않습니다.",
                            "data", rewardResponse
                    )
            );
        } else {
            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "리워드 조회에 성공했습니다.",
                            "data", rewardResponse
                    )
            );
        }

    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BENEFICIARY')")
    @PutMapping("/{rewardId}")
    public ResponseEntity<?> updateReward(
            @PathVariable("rewardId") int rewardId,
            @Valid @RequestBody RewardUpdateRequest request
    ) {
        try {
            RewardResponse rewardResponse = service.updateReward(rewardId, request);
            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "리워드 수정에 성공했습니다.",
                            "code", HttpStatus.OK.value(),
                            "data", rewardResponse
                    )
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "status", "error",
                                    "message", "리워드를 찾을 수 없습니다.",
                                    "code", HttpStatus.NOT_FOUND.value()
                            )
                    );
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BENEFICIARY')")
    @DeleteMapping("/{rewardId}")
    public ResponseEntity<?> deleteReward(
            @PathVariable("rewardId") int rewardId
    ) {
        try {
            service.deleteReward(rewardId);
            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "리워드 삭제에 성공했습니다.",
                            "code", HttpStatus.OK.value()
                    )
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "status", "error",
                                    "message", "리워드를 찾을 수 없습니다.",
                                    "code", HttpStatus.NOT_FOUND.value()
                            )
                    );
        }
    }
}
