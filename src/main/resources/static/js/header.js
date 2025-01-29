window.onload = function () {
    const authButtons = document.querySelector("#auth-buttons");

    if (!authButtons) {
        console.error("❌ auth-buttons 요소를 찾을 수 없습니다.");
        return;
    }

    const accessToken = localStorage.getItem("accessToken");

    console.log("🔹 토큰 존재 여부:", accessToken ? "있음" : "없음");

    if (accessToken) {
        try {
            const tokenPayload = JSON.parse(atob(accessToken.split(".")[1]));
            const userName = tokenPayload.sub || "알 수 없음";  // subject(name)
            const userRole = tokenPayload.role || "역할 없음"; // role 추출

            console.log("✅ 로그인 된 사용자:", userName, "역할:", userRole);

            // ✅ 역할을 한글로 변환
            const roleTranslation = {
                "BENEFICIARY": "수혜자",
                "SPONSOR": "후원자",
                "ADMIN": "관리자"
            };

            const translatedRole = roleTranslation[userRole] || "알 수 없는 역할";

            // ✅ 로그인 상태일 때: 사용자 이름 & 로그아웃 버튼 표시
            authButtons.innerHTML = `
                <span class="fw-bold text-primary">${userName} (${translatedRole})님</span>
                <a href="/mypage" class="btn btn-outline-primary">내 정보</a>
                <button onclick="logout()" class="btn btn-danger">로그아웃</button>
            `;
        } catch (error) {
            console.error("❌ JWT 디코딩 오류:", error);
        }
    }
};

function logout() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    window.location.href = "/";
}