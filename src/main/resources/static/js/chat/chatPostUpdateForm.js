/**
 * chatUpdateForm.js
 * 🎯 채팅방 수정 JS
 * - Hidden 필드에서 ID 추출 후 변경 데이터를 JSON으로 서버 전송
 */

$(document).ready(() => {

    /** 수정 폼 제출 이벤트 핸들러 */
    $('#chatUpdateForm').on('submit', e => {
        e.preventDefault();

        const performanceId = $('#perfId').val();
        const chatId = $('#chatId').val();

        const title = $('#title').val().trim();
        const content = $('#content').val().trim();
        const status = $('#status').val();

        if (!title || !content) {
            alert('제목과 내용을 입력해주세요.');
            return;
        }

        const data = { title, content, status };

        console.log('전송 데이터:', data);

        $.ajax({
            url: `/api/performances/${performanceId}/chats/${chatId}`,
            method: 'PATCH',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: res => {
                if (res.data || res.success) {
                    alert('수정이 완료되었습니다.');
                    window.location.href = `/performance/${performanceId}/chat/${chatId}`;
                } else {
                    alert(`수정 실패: ${res.message || '알 수 없는 이유'}`);
                }
            },
            error: xhr => {
                console.error('채팅방 수정 실패:', xhr);
                const errorMsg = xhr.responseJSON?.message || xhr.responseText || '서버 에러가 발생했습니다.';
                alert(`에러 발생: ${errorMsg}`);
            }
        });
    });

    /** 뒤로가기 버튼 클릭 */
    $('#backBtn').on('click', () => window.history.back());

});
