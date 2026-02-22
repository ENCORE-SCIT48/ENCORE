document.addEventListener('DOMContentLoaded', function() {
    // 1. 성공 메시지 처리
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('success') === 'true') {
        // 브라우저 기본 alert 대신 커스텀 알림이 있다면 여기 적용 가능
        alert('프로필이 안전하게 저장되었습니다! 🎉');

        // URL에서 success 파라미터 제거 (새로고침 시 중복 알림 방지)
        const cleanUrl = window.location.pathname;
        window.history.replaceState({}, document.title, cleanUrl);
    }

    // 2. 이미지 로딩 실패 시 기본 이미지 처리
    const profileImg = document.getElementById('profile-img');
    if (profileImg) {
        profileImg.onerror = function() {
            this.src = '/image/default-profile.png';
        };
    }
});
