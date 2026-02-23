document.addEventListener('DOMContentLoaded', function() {

    // 1. 저장 성공 알림 처리
    // URL에 ?success=true가 포함되어 있으면 알림을 띄웁니다.
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('success') === 'true') {
        // 브라우저 기본 알림창 사용
        alert('아티스트 프로필이 성공적으로 저장되었습니다! 🎉');

        // 알림창을 띄운 후 URL에서 파라미터를 제거하여
        // 새로고침 시 알림이 다시 뜨는 것을 방지합니다.
        const cleanUrl = window.location.pathname;
        window.history.replaceState({}, document.title, cleanUrl);
    }

    // 2. 이미지 로드 실패 시 처리 (옵션)
    const profileImg = document.getElementById('profile-img');
    if (profileImg) {
        profileImg.addEventListener('error', function() {
            // 이미지 경로가 잘못되었거나 불러올 수 없을 때 기본 이미지로 대체
            this.src = '/image/default-profile.png';
        });
    }

    // 3. '이전으로' 링크 클릭 시 로직 (history.back이 안 먹힐 경우 대비)
    const backLink = document.querySelector('.back-link');
    if (backLink) {
        backLink.addEventListener('click', function(e) {
            if (document.referrer === "" || document.referrer.includes('/setup')) {
                // 이전 페이지가 없거나 바로 직전이 설정 페이지였다면 메인으로 보냄 (선택 사항)
                // e.preventDefault();
                // location.href = '/';
            }
        });
    }
});
