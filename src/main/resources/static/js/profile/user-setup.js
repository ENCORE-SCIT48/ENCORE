/**
 * [설명] 관람객 프로필 설정 페이지의 유효성 검사 및 UI 제어를 담당합니다.
 * 파일명: user-setup.js
 */
document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('fileInput');
    const previewImg = document.getElementById('preview');
    const setupForm = document.querySelector('form');

    // 1. 이미지 파일 미리보기
    if (fileInput) {
        fileInput.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file && file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = (event) => {
                    previewImg.src = event.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    }

    // 2. 관람객 전용 폼 제출 검증
    if (setupForm) {
        setupForm.addEventListener('submit', function(e) {
            const submitBtn = this.querySelector('.btn-save');
            const phoneInput = document.getElementById('phoneNumber');
            const location = document.getElementById('location');
            const genres = document.querySelectorAll('input[name="preferredGenres"]:checked');

            // 구현 주석: 한국 표준 전화번호 형식 검증 정규식
            const phoneRegex = /^\d{2,3}-\d{3,4}-\d{4}$/;
            let errorMessage = "";

            if (!phoneInput || !phoneInput.value) {
                errorMessage += "- 전화번호를 입력해주세요.\n";
            } else if (!phoneRegex.test(phoneInput.value)) {
                errorMessage += "- 올바른 전화번호 형식(010-0000-0000)이 아닙니다.\n";
            }

            if (genres.length === 0) {
                errorMessage += "- 선호 장르를 최소 하나 이상 선택해주세요.\n";
            }
            if (location && !location.value) {
                errorMessage += "- 활동 지역을 선택해주세요.\n";
            }

            if (errorMessage) {
                e.preventDefault();
                alert("관람객 정보를 확인해주세요:\n" + errorMessage);
                return;
            }

            // 중복 제출 방지
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerText = '저장 중...';
            }
        });
    }
});
