window.onload = function () {
    const authButtons = document.querySelector("#auth-buttons");

    if (!authButtons) {
        console.error(" auth-buttons 요소를 찾을 수 없습니다.");
        return;
    }


    const accessToken = localStorage.getItem("accessToken");

    console.log("🔹 토큰 존재 여부:", accessToken ? "있음" : "없음");

    if (accessToken) {
        try {
            const tokenPayload = JSON.parse(atob(accessToken.split(".")[1]));
            const userName = tokenPayload.sub || "알 수 없음";  // subject(name)
            const userRole = tokenPayload.role || "역할 없음"; // role 추출

<<<<<<< HEAD
            console.log("✅ 로그인 된 사용자:", userName, "역할:", userRole);

            // ✅ 역할을 한글로 변환
            const roleTranslation = {
                "BENEFICIARY": "수혜자",
                "SPONSOR": "후원자",
                "ADMIN": "관리자"
=======
            console.log(" 로그인 된 사용자:", userName, "역할:", userRole);

            //  역할을 한글로 변환
            const roleTranslation = {
                "ROLE_BENEFICIARY": "수혜자",
                "ROLE_SPONSOR": "후원자",
                "ROLE_ADMIN": "관리자"
>>>>>>> feature/be/users-profile
            };

            const translatedRole = roleTranslation[userRole] || "알 수 없는 역할";

<<<<<<< HEAD
            // ✅ 로그인 상태일 때: 사용자 이름 & 로그아웃 버튼 표시
            authButtons.innerHTML = `
                <span class="fw-bold text-primary">${userName} (${translatedRole})님</span>
                <a href="/mypage" class="btn btn-outline-primary">내 정보</a>
                <button onclick="logout()" class="btn btn-danger">로그아웃</button>
            `;
        } catch (error) {
            console.error("❌ JWT 디코딩 오류:", error);
=======
            //  로그인 상태일 때: 사용자 이름 & 로그아웃 버튼 표시
       authButtons.innerHTML = `
           <span class="fw-bold text-primary">${userName} (${translatedRole})님</span>
           <a href="/profile/${userName}" class="btn btn-outline-primary">내 정보</a>
           <button id="logout-button" class="btn btn-danger">로그아웃</button>
       `;

       // ✅ 로그아웃 버튼에 이벤트 리스너 추가
       document.getElementById("logout-button").addEventListener("click", logout);
        } catch (error) {
            console.error(" JWT 디코딩 오류:", error);
>>>>>>> feature/be/users-profile
        }
    }
};

<<<<<<< HEAD
function logout() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    window.location.href = "/";
=======
async function logout() {
    console.log("🔹 로그아웃 요청 중...");

    try {
        // ✅ 서버에 로그아웃 요청 보내기
        const response = await fetch("/api/users/logout", {
            method: "POST",
            headers: { "Content-Type": "application/json" }
        });

        if (!response.ok) {
            throw new Error("서버 로그아웃 실패");
        }

        console.log("✅ 서버 로그아웃 완료");

        // ✅ 클라이언트에서 토큰 삭제
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");

        // ✅ 로그아웃 후 로그인 페이지로 리디렉션
        window.location.href = "/api/users/login";
    } catch (error) {
        console.error("❌ 로그아웃 오류:", error);
        alert("로그아웃 중 문제가 발생했습니다.");
    }
>>>>>>> feature/be/users-profile
}