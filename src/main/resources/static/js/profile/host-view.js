/* host-view.js */
document.addEventListener('DOMContentLoaded', function() {
    // 1. URL 파라미터를 확인하여 성공 메시지 표시
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('success') === 'true') {
        // 부드러운 알림 처리를 위해 브라우저 alert 사용 (필요 시 토스트 메시지로 대체 가능)
        alert('프로필 설정이 안전하게 저장되었습니다! 🎉');

        // URL에서 파라미터 제거 (새로고침 시 알림 중복 방지)
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    // 2. 홈으로 돌아가기 버튼 클릭 시 간단한 로그 (분석용 등)
    const homeLink = document.querySelector('.btn-link');
    if (homeLink) {
        homeLink.addEventListener('click', function() {
            console.log('User returning to home from host profile view.');
        });
    }
});
