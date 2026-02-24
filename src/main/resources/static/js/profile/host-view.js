/**
 * [설명] 호스트 프로필 조회 페이지의 알림 처리 및 사용자 액션을 관리합니다.
 */
document.addEventListener('DOMContentLoaded', () => {

    // 1. 저장 성공 알림 처리
    // 구현 주석: URL 파라미터(success=true)를 확인하여 프로필 저장 완료 메시지를 출력함
    const urlParams = new URLSearchParams(window.location.search);

    if (urlParams.get('success') === 'true') {
        alert('호스트 프로필 설정이 성공적으로 저장되었습니다! 🎉');

        // 구현 주석: 브라우저 히스토리 조작을 통해 URL 파라미터를 제거하여 새로고침 시 중복 알림 방지
        const cleanUrl = window.location.pathname;
        window.history.replaceState({}, document.title, cleanUrl);
    }

    // 2. 홈으로 돌아가기 링크 액션
    const homeLink = document.querySelector('.btn-link');

    if (homeLink) {
        homeLink.addEventListener('click', () => {
            // 구현 주석: 운영 환경에서의 보안 및 가이드라인 준수를 위해 console.log는 제거함
            // 필요 시 비즈니스 로그 수집 로직으로 대체 가능
        });
    }
});
