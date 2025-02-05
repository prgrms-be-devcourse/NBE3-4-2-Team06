document.addEventListener("DOMContentLoaded", async function () {
    console.log("✅ DOMContentLoaded 실행됨!");

    const authButtons = document.querySelector("#auth-buttons");

    if (!authButtons) {
        console.error("❌ auth-buttons 요소를 찾을 수 없습니다. HTML에서 id='auth-buttons'가 있는지 확인하세요.");
        return;
    }

    const accessToken = localStorage.getItem("accessToken");

    console.log("🔹 토큰 존재 여부:", accessToken ? "있음" : "없음");

    if (accessToken) {
        try {
            console.log("🔍 저장된 토큰:", accessToken);

            const tokenPayload = JSON.parse(atob(accessToken.split(".")[1]));
            const userName = tokenPayload.sub || "알 수 없음";
            const userRole = tokenPayload.role || "역할 없음";

            console.log("✅ 로그인 된 사용자:", userName, "역할:", userRole);

            try {
                console.log(`🔎 자동으로 프로필 데이터를 요청합니다: /api/users/profile/${userName}`);
                const response = await authFetch(`/api/users/profile/${userName}`);

                if (!response.ok) {
                    throw new Error(`프로필 요청 실패: ${response.status}`);
                }

                const profileData = await response.json();
                console.log("✅ 자동으로 가져온 프로필 데이터:", profileData);

                if (profileData && profileData.data) {
                    console.log("✅ UI 업데이트 중...");
                    updateAuthUI(userName, userRole);
                } else {
                    console.warn("⚠️ 프로필 데이터가 비어있습니다.");
                }
            } catch (error) {
                console.error("❌ 프로필 데이터 요청 실패:", error);
            }
        } catch (error) {
            console.error("❌ JWT 디코딩 오류:", error);
        }
    }
});

/**
 * ✅ UI 업데이트 함수
 */
function updateAuthUI(userName, userRole) {
    const authButtons = document.querySelector("#auth-buttons");

    if (!authButtons) return;

    const roleTranslation = {
        "ROLE_BENEFICIARY": "수혜자",
        "ROLE_SPONSOR": "후원자",
        "ROLE_ADMIN": "관리자"
    };

    const translatedRole = roleTranslation[userRole] || "알 수 없는 역할";

    authButtons.innerHTML = `
       <span class="fw-bold text-primary">${userName} (${translatedRole})님</span>
       <a href="/profile/${userName}" class="btn btn-outline-primary">내 정보</a>
       <button id="logout-button" class="btn btn-danger">로그아웃</button>
   `;

    setTimeout(() => {
        const logoutButton = document.getElementById("logout-button");
        if (logoutButton) {
            logoutButton.addEventListener("click", logout);
            console.log("✅ 로그아웃 버튼 이벤트 리스너 추가됨!");
        } else {
            console.error("❌ 로그아웃 버튼을 찾을 수 없습니다.");
        }
    }, 500);
}

/**
 * ✅ 공통 fetch 함수 (모든 요청에 Authorization 헤더 자동 추가)
 */
async function authFetch(url, options = {}) {
    console.log("🔎 authFetch 실행됨:", url);

    const token = localStorage.getItem("accessToken");

    if (!token) {
        console.warn("❌ JWT 토큰 없음! 인증이 필요한 요청을 보내지 못함.");
        alert("세션이 만료되었습니다. 다시 로그인해주세요.");
        window.location.href = "/api/users/login";
        throw new Error("인증 토큰이 없습니다. 로그인 후 다시 시도하세요.");
    }

    const headers = {
        ...options.headers,
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
    };

    console.log(`📌 요청 보내는 중: ${url}`);
    console.log("📌 요청 헤더:", headers);

    try {
        const response = await fetch(url, { ...options, headers });

        console.log("📌 서버 응답 상태 코드:", response.status);

        if (response.status === 403) {
            console.error("❌ 403 Forbidden - 접근 권한 없음.");
            alert("접근 권한이 없습니다. 다시 로그인해주세요.");
            localStorage.removeItem("accessToken");
            setTimeout(() => window.location.href = "/api/users/login", 1500);
            throw new Error("접근 권한 없음 (403)");
        }

        return response; // ✅ JSON 변환 없이 응답 객체를 그대로 반환
    } catch (error) {
        console.error("❌ authFetch 내부 오류:", error);
        throw error;
    }
}

/**
 * ✅ 로그아웃 함수 (토큰 삭제 후 리디렉션)
 */
async function logout() {
    console.log("🔹 로그아웃 요청 중...");

    try {
        const response = await authFetch("/api/users/logout", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${localStorage.getItem("accessToken")}`,
                "Content-Type": "application/json"
            }
        });

        if (!response) {
            throw new Error("서버로부터 응답이 없습니다.");
        }

        const contentType = response.headers?.get("content-type");
        let responseData;

        if (contentType && contentType.includes("application/json")) {
            responseData = await response.json();
        } else {
            responseData = await response.text();
            console.warn("⚠️ 서버 응답이 JSON 형식이 아닙니다. 응답 내용:", responseData);
        }

        console.log("✅ 로그아웃 응답:", responseData);

        if (!response.ok) {
            throw new Error(responseData?.message || `서버 오류: ${response.status} ${response.statusText}`);
        }

        if (!responseData || responseData.status !== "success") {
            console.warn("⚠️ 로그아웃 성공 상태가 없음, 서버 응답 확인 필요:", responseData);
        }

        // ✅ 토큰 삭제
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");

        // ✅ 메인 페이지로 이동
        window.location.href = "/";
    } catch (error) {
        console.error("❌ 로그아웃 오류:", error);
        alert(`로그아웃 중 문제가 발생했습니다: ${error.message}`);
    }
}
