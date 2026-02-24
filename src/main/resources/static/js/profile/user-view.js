/**
 * [설명] 관람객 프로필 조회 페이지의 UI 제어 및 알림 처리를 담당합니다.
 * 파일명: user-view.js
 */
document.addEventListener('DOMContentLoaded', () => {

    // 1. 프로필 저장 성공 알림
    // 구현 주석: URL 파라미터 체크를 통한 저장 완료 피드백 제공
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('success') === 'true') {
        alert('관람객 프로필이 성공적으로 업데이트되었습니다! 🎉');

        // URL 파라미터 제거하여 새로고침 시 중복 알림 방지
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    // 2. 관람객 이미지 로드 실패 시 처리
    const userImg = document.getElementById('user-profile-img');
    if (userImg) {
        userImg.addEventListener('error', function() {
            // 구현 주석: 이미지 부재 시 기본 아바타 이미지로 대체
            this.src = '/image/default-user.png';
            console.warn('[UserView] 관람객 이미지를 찾을 수 없어 기본 이미지로 대체합니다.');
        });
    }

    // 3. 홈으로 이동 로그 (필요 시)
    const homeBtn = document.querySelector('.btn-to-home');
    homeBtn?.addEventListener('click', () => {
        // 구현 주석: 단순 페이지 이동 외 추가 액션 필요 시 작성
    });
});
