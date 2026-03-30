// =============================================
// 내 정보 페이지
// =============================================

document.addEventListener('DOMContentLoaded', function () {

    // 탭 전환
    initTabs();

    // 회원 정보 로드
    loadMyInfo();

    // 저장 버튼
    document.getElementById('saveInfoBtn')
        .addEventListener('click', saveInfo);

    // 비밀번호 변경 버튼
    document.getElementById('changePasswordBtn')
        .addEventListener('click', changePassword);

    // 회원 탈퇴 버튼
    document.getElementById('deactivateBtn')
        .addEventListener('click', deactivate);
});

// =============================================
// 탭 전환
// =============================================
function initTabs() {
    document.querySelectorAll('.side-nav-item').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const tab = this.dataset.tab;

            // 버튼 활성화
            document.querySelectorAll('.side-nav-item')
                .forEach(b => b.classList.remove('active'));
            this.classList.add('active');

            // 섹션 전환
            document.querySelectorAll('.tab-section')
                .forEach(s => s.classList.remove('active'));
            document.getElementById('tab-' + tab)
                .classList.add('active');
        });
    });
}

// =============================================
// 내 정보 로드
// GET /api/members/me
// =============================================
function loadMyInfo() {
    fetch('/api/members/me')
        .then(function (res) {
            if (res.status === 401) {
                window.location.href = '/login';
                return;
            }
            return res.json();
        })
        .then(function (data) {
            if (!data) return;
            renderMyInfo(data);
        })
        .catch(function () {
            showToast('정보를 불러오는 중 오류가 발생했습니다.', 'error');
        });
}

function renderMyInfo(data) {
    // 사이드바 프로필
    document.getElementById('profileAvatar').textContent =
        data.nickname ? data.nickname.charAt(0).toUpperCase() : '?';
    document.getElementById('profileName').textContent  = data.nickname || '';
    document.getElementById('profileEmail').textContent = data.email || '';
    document.getElementById('profilePoint').textContent =
        (data.point || 0).toLocaleString();

    // 폼 입력값
    document.getElementById('infoEmail').value         = data.email || '';
    document.getElementById('infoName').value          = data.name || '';
    document.getElementById('infoNickname').value      = data.nickname || '';
    document.getElementById('infoPhone').value         = data.phone || '';
    document.getElementById('infoZipCode').value       = data.zipCode || '';
    document.getElementById('infoAddress').value       = data.address || '';
    document.getElementById('infoAddressDetail').value = data.addressDetail || '';
}

// =============================================
// 내 정보 수정
// PUT /api/members/me
// =============================================
function saveInfo() {
    const nickname      = document.getElementById('infoNickname').value.trim();
    const phone         = document.getElementById('infoPhone').value.trim();
    const zipCode       = document.getElementById('infoZipCode').value.trim();
    const address       = document.getElementById('infoAddress').value.trim();
    const addressDetail = document.getElementById('infoAddressDetail').value.trim();

    // 유효성 검사
    if (!nickname) {
        setHint('nicknameHint', '닉네임을 입력해주세요.', 'error');
        return;
    }

    if (phone && !/^\d{10,11}$/.test(phone)) {
        setHint('phoneHint', '올바른 전화번호 형식이 아닙니다.', 'error');
        return;
    }

    clearHints();

    const saveBtn = document.getElementById('saveInfoBtn');
    setLoading(saveBtn, true, '저장');

    fetch('/api/members/me', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            nickname,
            phone:         phone || null,
            zipCode:       zipCode || null,
            address:       address || null,
            addressDetail: addressDetail || null
        })
    })
    .then(function (res) {
        if (res.ok) {
            return res.json().then(function (data) {
                renderMyInfo(data);
                showToast('정보가 저장되었습니다.', 'success');
            });
        } else {
            return res.json().then(function (data) {
                showToast(data.message || '저장에 실패했습니다.', 'error');
            });
        }
    })
    .catch(function () {
        showToast('서버와 통신 중 오류가 발생했습니다.', 'error');
    })
    .finally(function () {
        setLoading(saveBtn, false, '저장');
    });
}

// =============================================
// 비밀번호 변경
// PUT /api/members/password
// =============================================
function changePassword() {
    const currentPassword    = document.getElementById('currentPassword').value;
    const newPassword        = document.getElementById('newPassword').value;
    const newPasswordConfirm = document.getElementById('newPasswordConfirm').value;

    clearHints();

    let valid = true;

    if (!currentPassword) {
        setHint('currentPasswordHint', '현재 비밀번호를 입력해주세요.', 'error');
        valid = false;
    }

    if (!newPassword) {
        setHint('newPasswordHint', '새 비밀번호를 입력해주세요.', 'error');
        valid = false;
    } else if (newPassword.length < 8) {
        setHint('newPasswordHint', '비밀번호는 8자 이상이어야 합니다.', 'error');
        valid = false;
    }

    if (!newPasswordConfirm) {
        setHint('newPasswordConfirmHint', '새 비밀번호 확인을 입력해주세요.', 'error');
        valid = false;
    } else if (newPassword !== newPasswordConfirm) {
        setHint('newPasswordConfirmHint', '비밀번호가 일치하지 않습니다.', 'error');
        valid = false;
    }

    if (!valid) return;

    const changeBtn = document.getElementById('changePasswordBtn');
    setLoading(changeBtn, true, '비밀번호 변경');

    fetch('/api/members/password', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currentPassword, newPassword })
    })
    .then(function (res) {
        if (res.ok) {
            showToast('비밀번호가 변경되었습니다.', 'success');
            document.getElementById('currentPassword').value    = '';
            document.getElementById('newPassword').value        = '';
            document.getElementById('newPasswordConfirm').value = '';
        } else {
            return res.json().then(function (data) {
                showToast(data.message || '비밀번호 변경에 실패했습니다.', 'error');
            });
        }
    })
    .catch(function () {
        showToast('서버와 통신 중 오류가 발생했습니다.', 'error');
    })
    .finally(function () {
        setLoading(changeBtn, false, '비밀번호 변경');
    });
}

// =============================================
// 회원 탈퇴
// DELETE /api/members/me
// =============================================
function deactivate() {
    if (!confirm('정말 탈퇴하시겠습니까?\n탈퇴 후 복구가 불가합니다.')) return;

    fetch('/api/members/me', { method: 'DELETE' })
        .then(function (res) {
            if (res.ok) {
                showToast('회원 탈퇴가 완료되었습니다.', 'info');
                setTimeout(function () {
                    window.location.href = '/login';
                }, 1200);
            } else {
                return res.json().then(function (data) {
                    showToast(data.message || '탈퇴 처리에 실패했습니다.', 'error');
                });
            }
        })
        .catch(function () {
            showToast('서버와 통신 중 오류가 발생했습니다.', 'error');
        });
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

function clearHints() {
    document.querySelectorAll('.form-hint').forEach(function (el) {
        el.textContent = '';
        el.className = 'form-hint';
    });
}

function setLoading(btn, loading, text) {
    if (!btn) return;
    btn.disabled = loading;
    btn.innerHTML = loading ? '<span class="spinner"></span>' : text;
}
