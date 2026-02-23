document.addEventListener('DOMContentLoaded', function() {
    const bizInput = document.getElementById('businessNumber');
    const phoneInput = document.getElementById('contactNumber');
    const dateInput = document.getElementById('openingDate');
    const checkBtn = document.getElementById('btnCheckBiz');
    const setupForm = document.getElementById('hostSetupForm');

    dateInput?.addEventListener('input', function() {
        // 숫자 이외의 문자 제거
        let val = this.value.replace(/[^0-9]/g, '');

        // 8자리까지만 제한
        if (val.length > 8) {
            val = val.substring(0, 8);
        }
        this.value = val;
    });
    // 1. 입력 양식 강제 (사업자번호: 000-00-00000)
    bizInput?.addEventListener('input', function() {
        let val = this.value.replace(/[^0-9]/g, '');
        if (val.length <= 3) this.value = val;
        else if (val.length <= 5) this.value = val.substring(0, 3) + '-' + val.substring(3);
        else this.value = val.substring(0, 3) + '-' + val.substring(3, 5) + '-' + val.substring(5, 10);
    });

    // 2. 입력 양식 강제 (전화번호)
    phoneInput?.addEventListener('input', function() {
        let val = this.value.replace(/[^0-9]/g, '');
        if (val.length < 4) this.value = val;
        else if (val.length < 7) this.value = val.substring(0, 3) + '-' + val.substring(3);
        else if (val.length < 11) this.value = val.substring(0, 3) + '-' + val.substring(3, 6) + '-' + val.substring(6);
        else this.value = val.substring(0, 3) + '-' + val.substring(3, 7) + '-' + val.substring(7, 11);
    });

    // 3. 사업자 조회 API 호출 (Step 1 & 2 통합)
    checkBtn?.addEventListener('click', async function() {
        const bizNum = bizInput.value;
        if (bizNum.length < 12) return alert('올바른 사업자 번호를 입력해주세요.');

        try {
            checkBtn.disabled = true;
            const response = await fetch('/api/host/check-status', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ businessNumber: bizNum })
            });
            const res = await response.json();

            if (response.ok) {
                if (confirm(`[조회 결과]\n상태: ${res.data.b_stt}\n유형: ${res.data.tax_type}\n\n이 정보로 인증을 확정하시겠습니까?`)) {
                    // 인증 확정 호출
                    const confirmRes = await fetch('/api/host/verify-confirm', {
                        method: 'PATCH',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ businessNumber: bizNum })
                    });
                    if (confirmRes.ok) {
                        alert('인증되었습니다! 페이지를 새로고침합니다.');
                        location.reload();
                    }
                }
            } else {
                alert(res.message);
            }
        } catch (e) {
            alert('인증 중 오류가 발생했습니다.');
        } finally {
            checkBtn.disabled = false;
        }
    });

    // 4. 최종 제출 전 인증 체크
    setupForm?.addEventListener('submit', function(e) {
        const isVerified = document.getElementById('isVerifiedHidden').value === 'true';
        if (!isVerified) {
            e.preventDefault();
            alert('사업자 인증을 먼저 완료해주세요.');
        }
    });

    // 5. 이미지 미리보기
    document.getElementById('fileInput')?.addEventListener('change', function(e) {
        const reader = new FileReader();
        reader.onload = (ev) => document.getElementById('preview').src = ev.target.result;
        reader.readAsDataURL(e.target.files[0]);
    });
});
