package Funding.Startreum.domain.reward.entity;

import Funding.Startreum.domain.project.Project;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "rewards")
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rewardId; // 리워드 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // 프로젝트 ID

    private String description; // 리워드 설명

    private BigDecimal amount; // 리워드 최소 기준 금액

    private LocalDateTime createdAt; // 생성 일자

    private LocalDateTime updatedAt; // 수정 일자
}
