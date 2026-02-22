document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('fileInput');
    const previewImg = document.getElementById('preview');
    const setupForm = document.querySelector('form');
    const phoneInput = document.getElementById('phoneNumber');

    // 1. 이미지 파일 선택 시 미리보기
    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                if (!file.type.startsWith('image/')) {
                    alert('이미지 파일만 업로드 가능합니다.');
                    fileInput.value = ''; // input 비우기
                    return;
                }
                const reader = new FileReader();
                reader.onload = function(event) {
                    previewImg.src = event.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    }

    // 2. 전화번호 자동 하이픈 (UX 최적화)
    if (phoneInput) {
        phoneInput.addEventListener('input', function(e) {
            let val = e.target.value.replace(/[^0-9]/g, '');
            if (val.length > 3 && val.length <= 7) {
                val = val.substring(0, 3) + '-' + val.substring(3);
            } else if (val.length > 7) {
                val = val.substring(0, 3) + '-' + val.substring(3, 7) + '-' + val.substring(7);
            }
            e.target.value = val.substring(0, 13); // 최대 13자 (010-0000-0000)
        });
    }

    // 3. 폼 제출 통합 관리 (유효성 검사 + 중복 클릭 방지)
    if (setupForm) {
        setupForm.addEventListener('submit', function(e) {
            const genres = document.querySelectorAll('input[name="preferredGenres"]:checked');
            const types = document.querySelectorAll('input[name="preferredPerformanceTypes"]:checked');
            const phone = phoneInput ? phoneInput.value : "";
            const location = document.getElementById('location').value;
            const submitBtn = this.querySelector('.btn-save');

            let errorMessage = "";

            // 유효성 체크 로직
            if (!phone || phone.length < 12) errorMessage += "- 올바른 전화번호를 입력해주세요.\n";
            if (!location) errorMessage += "- 지역을 선택해주세요.\n";
            if (genres.length === 0) errorMessage += "- 선호 장르를 최소 하나 이상 선택해주세요.\n";
            if (types.length === 0) errorMessage += "- 선호 공연 형태를 최소 하나 이상 선택해주세요.\n";

            if (errorMessage) {
                e.preventDefault(); // 제출 중단
                alert("입력 정보를 확인해주세요:\n" + errorMessage);
                return; // 함수 종료
            }

            // 모든 검증 통과 시 버튼 비활성화 (중복 클릭 방지)
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerText = '저장 중...';
            }
        });
    }

    // 4. 저장 성공 알림 처리 (URL 파라미터 확인)
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('success') === 'true') {
        alert('프로필 정보가 성공적으로 저장되었습니다! 🎉');
        window.history.replaceState({}, document.title, window.location.pathname);
    }
});
