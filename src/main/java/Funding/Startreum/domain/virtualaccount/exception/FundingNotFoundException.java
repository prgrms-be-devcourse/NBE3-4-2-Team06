package Funding.Startreum.domain.virtualaccount.exception;

// TODO 임시 API 반환 떄문에 만듦, 도메인 분리 및 논의 필요
public class FundingNotFoundException extends RuntimeException {

    public FundingNotFoundException(int fundingId) {
        super("펀딩 내역을 찾을 수 없습니다. 펀딩 ID: : " + fundingId);
    }

}
