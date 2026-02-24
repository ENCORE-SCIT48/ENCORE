/**
 * [설명] 아티스트(퍼포머) 프로필 조회 페이지의 UI 제어 및 알림 처리를 담당합니다.
 * 파일명: performer-view.js
 */
/**
 * [설명] 아티스트(퍼포머) 프로필 조회 페이지의 UI 제어 및 알림 처리를 담당합니다.
 */
document.addEventListener('DOMContentLoaded', () => {

    // 1. 아티스트 프로필 저장 성공 알림
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('success') === 'true') {
        alert('아티스트 프로필이 성공적으로 반영되었습니다! 멋진 공연을 기대할게요! 🎤');

        // 구현 주석: URL에서 파라미터 제거하여 새로고침 시 중복 알림 방지
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    // 2. 아티스트 프로필 이미지 로드 실패 시 처리
    const performerImg = document.getElementById('performer-profile-img');
    if (performerImg) {
        performerImg.addEventListener('error', function() {
            // 구현 주석: 이미지 로드 실패 시 아티스트 전용 기본 이미지로 대체
            this.src = '/image/default-profile.png';
            console.warn('[PerformerView] 아티스트 이미지를 로드할 수 없습니다.');
        });
    }
});
