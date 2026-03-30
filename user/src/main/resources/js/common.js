// =============================================
// 공통 유틸리티
// =============================================

/**
 * 토스트 메시지 표시
 * @param {string} message 메시지
 * @param {string} type    'success' | 'error' | 'info'
 */
function showToast(message, type = 'info') {
    let toast = document.getElementById('toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toast';
        toast.className = 'toast';
        document.body.appendChild(toast);
    }

    toast.textContent = message;
    toast.className = `toast ${type} show`;

    setTimeout(() => {
        toast.className = `toast ${type}`;
    }, 3000);
}

/**
 * 네비게이션 로그인 상태 렌더링
 * 쿠키에 accessToken 있으면 로그인 상태로 표시
 */
function renderNav() {
    const navAuth = document.getElementById('navAuth');
    if (!navAuth) return;

    // 쿠키에서 accessToken 확인
    const hasToken = document.cookie
            .split(';')
            .some(c => c.trim().startsWith('accessToken='));

    if (hasToken) {
        // 로그인 상태
        navAuth.innerHTML = `
            <a href="/my-page" class="nav-link">내 정보</a>
            <button class="nav-btn nav-btn-outline" onclick="logout()">로그아웃</button>
        `;
    } else {
        // 비로그인 상태
        navAuth.innerHTML = `
            <a href="/login" class="nav-btn nav-btn-outline">로그인</a>
            <a href="/signup" class="nav-btn nav-btn-primary">회원가입</a>
        `;
    }
}

/**
 * 로그아웃
 * POST /api/auth/logout → 메인 페이지 이동
 */
function logout() {
    fetch('/api/auth/logout', { method: 'POST' })
        .then(() => {
            showToast('로그아웃 되었습니다.', 'success');
            setTimeout(() => window.location.href = '/login', 800);
        })
        .catch(() => {
            showToast('로그아웃 처리 중 오류가 발생했습니다.', 'error');
        });
}

/**
 * API 에러 메시지 추출
 * @param {Response} response fetch 응답 객체
 */
async function getErrorMessage(response) {
    try {
        const data = await response.json();
        return data.message || '오류가 발생했습니다.';
    } catch {
        return '오류가 발생했습니다.';
    }
}

// 페이지 로드 시 네비게이션 렌더링
document.addEventListener('DOMContentLoaded', renderNav);
