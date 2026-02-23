document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('fileInput');
    const previewImg = document.getElementById('preview');
    const setupForm = document.querySelector('form');

    // 1. 이미지 파일 미리보기 (공통)
    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file && file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = (event) => previewImg.src = event.target.result;
                reader.readAsDataURL(file);
            }
        });
    }

    // 2. 폼 제출 통합 관리 (유효성 검사)
    if (setupForm) {
        setupForm.addEventListener('submit', function(e) {
            const submitBtn = this.querySelector('.btn-save');
            let errorMessage = "";

            // [퍼포머 전용 체크]
            const stageName = document.getElementById('stageName');
            const activityArea = document.getElementById('activityArea'); // 지역 선택 select box

            if (stageName) { // 퍼포머 페이지인 경우
                const categories = document.querySelectorAll('input[name="categories"]:checked');
                const parts = document.querySelectorAll('input[name="part"]:checked');

                if (!stageName.value.trim()) errorMessage += "- 활동명(Stage Name)을 입력해주세요.\n";

                // 지역 선택 검증 추가
                if (activityArea && !activityArea.value) {
                    errorMessage += "- 활동 선호 지역을 선택해주세요.\n";
                }

                if (parts.length === 0) errorMessage += "- 활동 포지션을 최소 하나 이상 선택해주세요.\n";
                if (categories.length === 0) errorMessage += "- 주요 활동 장르를 최소 하나 이상 선택해주세요.\n";
            }

            // [관람객 전용 체크]
            const phoneInput = document.getElementById('phoneNumber');
            if (phoneInput) { // 관람객 페이지인 경우
                const genres = document.querySelectorAll('input[name="preferredGenres"]:checked');
                const location = document.getElementById('location'); // 관람객용 지역 필드명 확인 필요

                // [관람객 전용 체크 부분 수정]
                            if (phoneInput) {
                                const phoneRegex = /^\d{2,3}-\d{3,4}-\d{4}$/; // 010-1234-5678 형식 정규식
                                const genres = document.querySelectorAll('input[name="preferredGenres"]:checked');

                                if (!phoneInput.value) {
                                    errorMessage += "- 전화번호를 입력해주세요.\n";
                                } else if (!phoneRegex.test(phoneInput.value)) {
                                    errorMessage += "- 올바른 전화번호 형식(010-0000-0000)이 아닙니다.\n";
                                } if (genres.length === 0) errorMessage += "- 선호 장르를 선택해주세요.\n";
                if (location && !location.value) errorMessage += "- 활동 지역을 선택해주세요.\n";
            }

            if (errorMessage) {
                e.preventDefault();
                alert("입력 정보를 확인해주세요:\n" + errorMessage);
                return;
            }

            // 중복 클릭 방지
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerText = '저장 중...';
            }
        });
    }

    // 3. 성공 알림 (공통)
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('success') === 'true') {
        alert('프로필 정보가 성공적으로 저장되었습니다! 🎉');
        window.history.replaceState({}, document.title, window.location.pathname);
    }
});
