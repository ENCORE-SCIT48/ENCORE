/**
 * [설명] 아티스트(퍼포머) 프로필 설정 페이지의 유효성 검사 및 UI 제어를 담당합니다.
 * 파일명: performer-setup.js
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

    // 2. 아티스트 전용 폼 제출 검증
    if (setupForm) {
        setupForm.addEventListener('submit', function(e) {
            const submitBtn = this.querySelector('.btn-save');
            const stageName = document.getElementById('stageName');
            const activityArea = document.getElementById('activityArea');
            const categories = document.querySelectorAll('input[name="categories"]:checked');
            const parts = document.querySelectorAll('input[name="part"]:checked');

            let errorMessage = "";

            // 구현 주석: 아티스트 필수 입력 항목 검증
            if (!stageName || !stageName.value.trim()) {
                errorMessage += "- 활동명(Stage Name)을 입력해주세요.\n";
            }
            if (activityArea && !activityArea.value) {
                errorMessage += "- 활동 선호 지역을 선택해주세요.\n";
            }
            if (parts.length === 0) {
                errorMessage += "- 활동 포지션을 최소 하나 이상 선택해주세요.\n";
            }
            if (categories.length === 0) {
                errorMessage += "- 주요 활동 장르를 최소 하나 이상 선택해주세요.\n";
            }

            if (errorMessage) {
                e.preventDefault();
                alert("아티스트 정보를 확인해주세요:\n" + errorMessage);
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
