// =============================================
// 메인 페이지
// =============================================

document.addEventListener('DOMContentLoaded', function () {

    // 로그인 상태에 따라 히어로 버튼 변경
    const hasToken = document.cookie
        .split(';')
        .some(c => c.trim().startsWith('accessToken='));

    if (hasToken) {
        // 로그인 상태 → 도서 검색, 내 정보 버튼 표시
        document.getElementById('heroActions').innerHTML = `
            <a href="http://localhost:8080" class="btn btn-hero-primary">도서 검색</a>
            <a href="/my-page" class="btn btn-hero-mypage">내 정보</a>
        `;
    }
});
