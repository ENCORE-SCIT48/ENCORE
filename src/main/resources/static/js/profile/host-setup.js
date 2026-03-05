/**
 * [설명] 호스트 설정 페이지의 입력 마스킹, 사업자 인증 API 연동 및 유효성 검사를 담당합니다.
 */
document.addEventListener('DOMContentLoaded', () => {
    const bizInput = document.getElementById('businessNumber');
    const phoneInput = document.getElementById('contactNumber');
    const dateInput = document.getElementById('openingDate');
    const checkBtn = document.getElementById('btnCheckBiz');
    const setupForm = document.getElementById('hostSetupForm'); // HTML <form id="hostSetupForm"> 필요
    const verifiedHidden = document.querySelector('input[name="verified"]');
    const verifyBadge = document.getElementById('verificationBadge');

    // [수정] 재할당을 위해 const를 let으로 변경
    let originalBizNum = bizInput?.value;

    // [0] 사업자 번호 수정 감지 로직
    bizInput?.addEventListener('input', function() {
        // 하이픈이 포함된 마스킹 결과값 기준으로 비교
        if (this.value !== originalBizNum) {
            if (verifyBadge) verifyBadge.style.display = 'none';
            if (verifiedHidden) verifiedHidden.value = 'false';
        } else {
            if (verifyBadge) verifyBadge.style.display = 'block';
            if (verifiedHidden) verifiedHidden.value = 'true';
        }
    });

    // [1] 개업일자 입력 제어 (8자리 숫자)
    dateInput?.addEventListener('input', function() {
        let val = this.value.replace(/[^0-9]/g, '');
        this.value = val.substring(0, 8);
    });

    // [2] 사업자 등록 번호 마스킹 (000-00-00000)
    bizInput?.addEventListener('input', function() {
        let val = this.value.replace(/[^0-9]/g, '');
        if (val.length <= 3) this.value = val;
        else if (val.length <= 5) this.value = val.substring(0, 3) + '-' + val.substring(3);
        else this.value = val.substring(0, 3) + '-' + val.substring(3, 5) + '-' + val.substring(5, 10);
    });

    // [3] 연락처 마스킹 (000-0000-0000)
    phoneInput?.addEventListener('input', function() {
        let val = this.value.replace(/[^0-9]/g, '');
        if (val.length < 4) this.value = val;
        else if (val.length < 7) this.value = val.substring(0, 3) + '-' + val.substring(3);
        else if (val.length < 11) this.value = val.substring(0, 3) + '-' + val.substring(3, 6) + '-' + val.substring(6);
        else this.value = val.substring(0, 3) + '-' + val.substring(3, 7) + '-' + val.substring(7, 11);
    });

    // [4] 사업자 조회 및 인증 확정 API 호출
    checkBtn?.addEventListener('click', async () => {
        const bizNum = bizInput.value;
        if (bizNum.length < 12) return alert('올바른 사업자 번호를 입력해주세요.');

        try {
            checkBtn.disabled = true;
            const response = await fetch('/api/host/check-status', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ businessNumber: bizNum }),
                credentials: 'include',
            });

            const res = await response.json();

            if (response.ok) {
                if (res.data.b_stt_cd !== '01') {
                    alert(`인증 불가 상태입니다.\n현재 상태: ${res.data.b_stt || '정보 없음'}`);
                    return;
                }

                if (confirm(`[조회 완료]\n상태: 정상(활동중)\n유형: ${res.data.tax_type}\n\n이 정보로 인증하시겠습니까?`)) {
                    const confirmRes = await fetch('/api/host/verify-confirm', {
                        method: 'PATCH',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ businessNumber: bizNum }),
                        credentials: 'include',
                    });

                    if (confirmRes.ok) {
                        alert('인증되었습니다!');
                        if (verifyBadge) {
                            verifyBadge.style.display = 'block';
                            verifyBadge.innerHTML = '<i class="bi bi-patch-check-fill"></i> 인증된 사업자입니다.';
                        }
                        if (verifiedHidden) verifiedHidden.value = 'true';
                        originalBizNum = bizInput.value; // 기준점 갱신
                    } else {
                        const errorData = await confirmRes.json();
                        alert(`인증 확정 실패: ${errorData.message}`);
                    }
                }
            } else {
                alert(`조회 실패: ${res.message || '정보를 확인할 수 없습니다.'}`);
            }
        } catch (e) {
            alert('서버 통신 중 오류가 발생했습니다.');
        } finally {
            checkBtn.disabled = false;
        }
    });

// [5] 최종 제출 전 통합 유효성 검사 (빈칸 + 인증 체크)
setupForm?.addEventListener('submit', (e) => {
    // 1. 필수 입력 필드 매핑 (ID: 필드명)
    const requiredFields = [
        { id: 'businessNumber', name: '사업자 번호' },
        { id: 'organizationName', name: '단체명' },
        { id: 'representativeName', name: '대표자명' },
        { id: 'openingDate', name: '개업일자' },
        { id: 'contactNumber', name: '대표 연락처' }
    ];

    // 2. 빈칸 검사
    for (const field of requiredFields) {
        const input = document.getElementById(field.id);
        if (!input || !input.value.trim()) {
            e.preventDefault();
            alert(`${field.name}을(를) 입력해주세요.`);
            input.focus();
            return false;
        }
    }

    // 3. 사업자 인증 여부 최종 체크
    // hidden 필드의 id가 'verified'인 경우 (th:field="*{verified}"는 id="verified"로 생성됨)
    const vHidden = document.getElementById('verified');
    if (!vHidden || vHidden.value !== 'true') {
        e.preventDefault();
        alert('사업자 인증을 완료해주세요.');
        document.getElementById('businessNumber').focus();
        return false;
    }

    // 4. 개업일자 자릿수 검사 (선택 사항)
    const dateVal = document.getElementById('openingDate').value;
    if (dateVal.length !== 8) {
        e.preventDefault();
        alert('개업일자 8자리(YYYYMMDD)를 정확히 입력해주세요.');
        document.getElementById('openingDate').focus();
        return false;
    }

    // 모든 검사 통과 시 제출 진행
});

    // [6] 이미지 미리보기
    document.getElementById('fileInput')?.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (ev) => document.getElementById('preview').src = ev.target.result;
            reader.readAsDataURL(file);
        }
    });

    if (bizInput && bizInput.value) {
            bizInput.dispatchEvent(new Event('input'));
        }
});
