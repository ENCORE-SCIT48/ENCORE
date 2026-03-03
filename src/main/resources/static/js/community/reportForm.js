/**
     * [설명] 신고 페이지의 인터랙션 및 API 통신을 담당합니다.
     */
    const ReportHandler = {
        init: function() {
            this.cacheDom();
            this.bindEvents();
        },

        cacheDom: function() {
            this.reportForm = document.getElementById('reportForm');
            this.reasonSelect = document.getElementById('reportReasonSelect');
            this.etcWrapper = document.getElementById('etcReasonWrapper');
            this.reasonDetail = document.getElementById('reasonDetail');
        },

        bindEvents: function() {
            this.reasonSelect.addEventListener('change', (e) => {
                this.toggleEtcInput(e.target.value);
            });

            this.reportForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.submitReport();
            });
        },

        /**
         * [설명] 기타 사유 선택 시 입력창을 토글합니다.
         */
        toggleEtcInput: function(selectedValue) {
            if (selectedValue === 'ETC') {
                this.etcWrapper.style.display = 'block';
                this.reasonDetail.setAttribute('required', 'required');
                this.reasonDetail.focus();
            } else {
                this.etcWrapper.style.display = 'none';
                this.reasonDetail.removeAttribute('required');
                this.reasonDetail.value = '';
            }
        },

        /**
         * [설명] 신고 데이터를 서버로 전송합니다.
         */
        submitReport: function() {
            const payload = {
                targetId: document.getElementById('targetId').value,
                targetType: document.getElementById('targetType').value,
                reason: this.reasonSelect.value,
                reasonDetail: this.reasonDetail.value
            };

            // 공통 응답 규격(CommonResponse)에 따른 처리 예시
            fetch('/api/community/reports', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify(payload)
            })
            .then(response => response.json())
            .then(result => {
                             // 서버의 CommonResponse 구조에 맞게 수정
                             // result.success가 true이거나 result.code가 200인 경우 성공으로 간주
                             if (result.success || result.code === 200) {
                                 alert(result.message || '신고가 정상적으로 접수되었습니다.');

                                 // 이전 페이지로 확실하게 이동하기
                                 if (document.referrer) {
                                     location.href = document.referrer;
                                 } else {
                                     window.history.back();
                                 }
                             } else {
                                 // 실패했을 때 (예: 이미 신고한 대상 등)
                                 alert('처리 중 오류 발생: ' + result.message);
                             }
                         })
            .catch(error => {
                console.error('ERROR:', error);
                alert('네트워크 오류가 발생했습니다.');
            });
        }
    };

    // DOM 로드 완료 후 실행
    document.addEventListener('DOMContentLoaded', () => {
        ReportHandler.init();
    });
