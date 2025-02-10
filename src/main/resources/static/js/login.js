 document.getElementById('loginForm').onsubmit = async (event) => {
        event.preventDefault();

        const name = document.getElementById('name').value;
        const password = document.getElementById('password').value;

        try {
            const response = await fetch('/api/users/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ name, password }),
            });

            if (!response.ok) {
                const error = await response.json();
                alert(`로그인 실패: ${error.error}`);
                return;
            }

            const user = await response.json();
            alert(`로그인 성공! 환영합니다, ${user.userName}님.`);

            localStorage.setItem("accessToken", user.accessToken);
            localStorage.setItem("refreshToken", user.refreshToken);

            window.location.href = '/';
        } catch (err) {
            alert('네트워크 오류가 발생했습니다.');
        }
    };