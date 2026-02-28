/**
 * chatPostUpdateForm.js
 * [설명] 채팅방 수정 폼 상태 관리 및 제출 처리
 */

$(document).ready(() => {
    const statusButtons = $('.status-btn');
    const statusInput = $('#status');
    const chatUpdateForm = $('#chatUpdateForm');
    const backBtn = $('#backBtn');

    // ===== 초기 상태 버튼 활성화 (수정일 경우) =====
    const currentStatus = statusInput.val();
    statusButtons.each(function () {
        if ($(this).data('status') === currentStatus) {
            $(this).addClass('active');
        } else {
            $(this).removeClass('active');
        }
    });

    // ===== 상태 버튼 클릭 이벤트 =====
    statusButtons.on('click', function () {
        statusButtons.removeClass('active'); // 모두 비활성화
        $(this).addClass('active');          // 클릭한 버튼 활성화
        statusInput.val($(this).data('status')); // hidden input 값 변경
    });

    /**
     * [설명] 채팅방 수정 폼 제출 이벤트 처리
     */
    chatUpdateForm.on('submit', (e) => {
        e.preventDefault();

        const performanceId = $('#perfId').val();
        const chatId = $('#chatId').val();
        const title = $('#title').val().trim();
        const content = $('#content').val().trim();
        const status = statusInput.val();

        // ===== 유효성 체크 =====
        if (!title || title.length < 2 || title.length > 100) {
            alert('제목은 2~100자 사이로 입력해주세요.');
            return;
        }

        if (!performanceId || !title || !content || !maxMember) {
                    alert('모든 입력값을 올바르게 입력해주세요.');
                    return;
                }

        if (content.length > 200) {
                alert('내용은 200자 이하로 입력해주세요.');
                return;
            }

        if (!status) {
            alert('상태를 반드시 선택해주세요.');
            return;
        }

        const data = { title, content, status };
        console.log('전송 데이터:', data);

        // ===== AJAX 요청 =====
        $.ajax({
            url: `/api/performances/${performanceId}/chats/${chatId}`,
            method: 'PATCH',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: (res) => {
                if (res.success) {
                    alert('수정이 완료되었습니다.');
                    window.location.href = `/performances/${performanceId}/chat/${chatId}`;
                } else {
                    alert(`수정 실패: ${res.message || '알 수 없는 이유'}`);
                }
            },
            error: (xhr) => {
                console.error('채팅방 수정 실패:', xhr);
                const errorMsg = xhr.responseJSON?.message || xhr.responseText || '서버 에러가 발생했습니다.';
                alert(`에러 발생: ${errorMsg}`);
            }
        });
    });

    /** [설명] 뒤로가기 버튼 클릭 */
    backBtn.on('click', () => window.history.back());
});
