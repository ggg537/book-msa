// =============================================
// 회원가입 처리
// =============================================

// 중복 확인 상태
let emailChecked    = false;
let nicknameChecked = false;

document.addEventListener('DOMContentLoaded', function () {

    // 비밀번호 보기/숨기기
    const togglePw      = document.getElementById('togglePw');
    const passwordInput = document.getElementById('password');

    if (togglePw) {
        togglePw.addEventListener('click', function () {
            const isPassword = passwordInput.type === 'password';
            passwordInput.type = isPassword ? 'text' : 'password';
            togglePw.textContent = isPassword ? '🙈' : '👁';
        });
    }

    // 이메일 입력 시 중복 확인 초기화
    document.getElementById('email')
        .addEventListener('input', function () {
            emailChecked = false;
            setHint('emailHint', '', '');
        });

    // 닉네임 입력 시 중복 확인 초기화
    document.getElementById('nickname')
        .addEventListener('input', function () {
            nicknameChecked = false;
            setHint('nicknameHint', '', '');
        });

    // 이메일 중복 확인
    document.getElementById('checkEmailBtn')
        .addEventListener('click', checkEmail);

    // 닉네임 중복 확인
    document.getElementById('checkNicknameBtn')
        .addEventListener('click', checkNickname);

    // 회원가입 폼 제출
    document.getElementById('signupForm')
        .addEventListener('submit', function (e) {
            e.preventDefault();
            submitSignup();
        });
});

/**
 * 이메일 중복 확인
 * GET /api/auth/check-email?email=xxx
 */
function checkEmail() {
    const email = document.getElementById('email').value.trim();

    if (!email) {
        setHint('emailHint', '이메일을 입력해주세요.', 'error');
        return;
    }

    if (!isValidEmail(email)) {
        setHint('emailHint', '올바른 이메일 형식이 아닙니다.', 'error');
        return;
    }

    fetch(`/api/auth/check-email?email=${encodeURIComponent(email)}`)
        .then(res => res.json())
        .then(function (data) {
            if (data.available) {
                setHint('emailHint', '사용 가능한 이메일입니다.', 'success');
                emailChecked = true;
            } else {
                setHint('emailHint', '이미 사용 중인 이메일입니다.', 'error');
                emailChecked = false;
            }
        })
        .catch(function () {
            setHint('emailHint', '확인 중 오류가 발생했습니다.', 'error');
        });
}

/**
 * 닉네임 중복 확인
 * GET /api/auth/check-nickname?nickname=xxx
 */
function checkNickname() {
    const nickname = document.getElementById('nickname').value.trim();

    if (!nickname) {
        setHint('nicknameHint', '닉네임을 입력해주세요.', 'error');
        return;
    }

    fetch(`/api/auth/check-nickname?nickname=${encodeURIComponent(nickname)}`)
        .then(res => res.json())
        .then(function (data) {
            if (data.available) {
                setHint('nicknameHint', '사용 가능한 닉네임입니다.', 'success');
                nicknameChecked = true;
            } else {
                setHint('nicknameHint', '이미 사용 중인 닉네임입니다.', 'error');
                nicknameChecked = false;
            }
        })
        .catch(function () {
            setHint('nicknameHint', '확인 중 오류가 발생했습니다.', 'error');
        });
}

/**
 * 회원가입 API 호출
 * POST /api/auth/signup
 */
function submitSignup() {
    const email           = document.getElementById('email').value.trim();
    const password        = document.getElementById('password').value;
    const passwordConfirm = document.getElementById('passwordConfirm').value;
    const name            = document.getElementById('name').value.trim();
    const nickname        = document.getElementById('nickname').value.trim();
    const phone           = document.getElementById('phone').value.trim();

    // 유효성 검사
    if (!validateSignupForm(email, password, passwordConfirm, name, nickname, phone)) return;

    const signupBtn = document.getElementById('signupBtn');
    setLoading(signupBtn, true, '회원가입');

    fetch('/api/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, name, nickname, phone: phone || null })
    })
    .then(function (response) {
        if (response.ok) {
            showToast('회원가입이 완료되었습니다!', 'success');
            setTimeout(function () {
                window.location.href = '/login';
            }, 1000);
        } else {
            return response.json().then(function (data) {
                showToast(data.message || '회원가입에 실패했습니다.', 'error');
            });
        }
    })
    .catch(function () {
        showToast('서버와 통신 중 오류가 발생했습니다.', 'error');
    })
    .finally(function () {
        setLoading(signupBtn, false, '회원가입');
    });
}

/**
 * 회원가입 폼 유효성 검사
 */
function validateSignupForm(email, password, passwordConfirm, name, nickname, phone) {
    let valid = true;

    // 이메일
    if (!email) {
        setHint('emailHint', '이메일을 입력해주세요.', 'error');
        valid = false;
    } else if (!isValidEmail(email)) {
        setHint('emailHint', '올바른 이메일 형식이 아닙니다.', 'error');
        valid = false;
    } else if (!emailChecked) {
        setHint('emailHint', '이메일 중복 확인을 해주세요.', 'error');
        valid = false;
    }

    // 비밀번호
    if (!password) {
        setHint('passwordHint', '비밀번호를 입력해주세요.', 'error');
        valid = false;
    } else if (password.length < 8) {
        setHint('passwordHint', '비밀번호는 8자 이상이어야 합니다.', 'error');
        valid = false;
    }

    // 비밀번호 확인
    if (!passwordConfirm) {
        setHint('passwordConfirmHint', '비밀번호 확인을 입력해주세요.', 'error');
        valid = false;
    } else if (password !== passwordConfirm) {
        setHint('passwordConfirmHint', '비밀번호가 일치하지 않습니다.', 'error');
        valid = false;
    }

    // 이름
    if (!name) {
        setHint('nameHint', '이름을 입력해주세요.', 'error');
        valid = false;
    }

    // 닉네임
    if (!nickname) {
        setHint('nicknameHint', '닉네임을 입력해주세요.', 'error');
        valid = false;
    } else if (!nicknameChecked) {
        setHint('nicknameHint', '닉네임 중복 확인을 해주세요.', 'error');
        valid = false;
    }

    // 전화번호 (선택)
    if (phone && !/^\d{10,11}$/.test(phone)) {
        setHint('phoneHint', '올바른 전화번호 형식이 아닙니다. (숫자만 10~11자리)', 'error');
        valid = false;
    }

    return valid;
}

// =============================================
// 유틸
// =============================================

function setHint(id, message, type) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = message;
    el.className = `form-hint ${type}`;
}

function setLoading(btn, loading, text) {
    if (!btn) return;
    btn.disabled = loading;
    btn.innerHTML = loading
        ? '<span class="spinner"></span>'
        : text;
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}
