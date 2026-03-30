// =============================================
// 로그인 처리
// =============================================

document.addEventListener('DOMContentLoaded', function () {

    const loginForm = document.getElementById('loginForm');
    if (!loginForm) return;

    // 비밀번호 보기/숨기기
    const togglePw = document.getElementById('togglePw');
    const passwordInput = document.getElementById('password');

    if (togglePw) {
        togglePw.addEventListener('click', function () {
            const isPassword = passwordInput.type === 'password';
            passwordInput.type = isPassword ? 'text' : 'password';
            togglePw.textContent = isPassword ? '🙈' : '👁';
        });
    }

    // 로그인 폼 제출
    loginForm.addEventListener('submit', function (e) {
        e.preventDefault();

        const email    = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;

        // 유효성 검사
        if (!validateLoginForm(email, password)) return;

        submitLogin(email, password);
    });
});

/**
 * 로그인 폼 유효성 검사
 */
function validateLoginForm(email, password) {
    let valid = true;

    clearErrors();

    if (!email) {
        showFieldError('emailError', '이메일을 입력해주세요.');
        valid = false;
    } else if (!isValidEmail(email)) {
        showFieldError('emailError', '올바른 이메일 형식이 아닙니다.');
        valid = false;
    }

    if (!password) {
        showFieldError('passwordError', '비밀번호를 입력해주세요.');
        valid = false;
    }

    return valid;
}

/**
 * 로그인 API 호출
 * POST /api/auth/login
 */
function submitLogin(email, password) {
    const loginBtn = document.getElementById('loginBtn');

    setLoading(loginBtn, true);

    fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    })
    .then(function (response) {
        if (response.ok) {
            return response.json().then(function (data) {
                showToast('로그인 성공!', 'success');
                // 로그인 성공 → 도서 검색 페이지로 이동
                setTimeout(function () {
                    window.location.href = '/';
                }, 600);
            });
        } else {
            return response.json().then(function (data) {
                showToast(data.message || '로그인에 실패했습니다.', 'error');
            });
        }
    })
    .catch(function () {
        showToast('서버와 통신 중 오류가 발생했습니다.', 'error');
    })
    .finally(function () {
        setLoading(loginBtn, false);
    });
}

// =============================================
// 공통 유틸 (auth 전용)
// =============================================

function showFieldError(id, message) {
    const el = document.getElementById(id);
    if (el) {
        el.textContent = message;
        el.style.display = 'block';
    }
}

function clearErrors() {
    document.querySelectorAll('.form-hint.error').forEach(function (el) {
        el.textContent = '';
        el.style.display = 'none';
    });
    document.querySelectorAll('.form-input').forEach(function (el) {
        el.classList.remove('error');
    });
}

function setLoading(btn, loading) {
    if (!btn) return;
    if (loading) {
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span>';
    } else {
        btn.disabled = false;
        btn.textContent = btn.id === 'loginBtn' ? '로그인' : '회원가입';
    }
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}
