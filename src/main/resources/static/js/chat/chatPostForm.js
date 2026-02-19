/**
 * chatPostForm.js
 * 🎯 신규 채팅방 등록 JS
 * - 폼 데이터를 JSON으로 변환 후 비동기(Ajax) 요청
 */

$(document).ready(() => {

    $('#chatPostForm').on('submit', e => {
        e.preventDefault();

        const performanceId = $('#performanceId').val();
        const title = $('#title').val().trim();
        const content = $('#content').val().trim();
        const maxMember = Number($('#maxMember').val());

        if (!performanceId || !title || !content || !maxMember) {
            alert('모든 입력값을 올바르게 입력해주세요.');
            return;
        }

        const data = { performanceId: Number(performanceId), title, content, maxMember };

        $.ajax({
            url: `/api/performances/${performanceId}/chats`,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: res => {
                if (res.data) {
                    alert('채팅방이 생성되었습니다.');
                    window.location.href = `/performance/${performanceId}/chat/list`;
                } else {
                    alert(`실패: ${res.message}`);
                }
            },
            error: xhr => {
                console.error('채팅방 생성 실패:', xhr);

                const errorMessage = xhr.responseJSON?.message || xhr.responseText || '알 수 없는 에러가 발생했습니다.';

                switch (xhr.status) {
                    case 400:
                        alert(`입력값이 올바르지 않습니다:\n${errorMessage}`);
                        break;
                    case 401:
                        alert('로그인이 필요합니다.');
                        window.location.href = '/login';
                        break;
                    case 403:
                        alert('권한이 없습니다.');
                        break;
                    default:
                        alert(`에러 발생: ${errorMessage}`);
                }
            }
        });
    });

});
