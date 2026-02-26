/**
 * chatPostForm.js
 * 🎯 신규 채팅방 등록 JS
 * [설명] 폼 데이터를 JSON으로 변환 후 Ajax 요청
 */

$(document).ready(() => {
    const chatForm = $('#chatPostForm');
    const performanceInput = $('#performanceId');
    const titleInput = $('#title');
    const contentInput = $('#content');
    const maxMemberInput = $('#maxMember');

    /**
     * [설명] 채팅방 등록 폼 제출 이벤트
     */
    chatForm.on('submit', (e) => {
        e.preventDefault();

        const performanceId = performanceInput.val();
        const title = titleInput.val().trim();
        const content = contentInput.val().trim();
        const maxMember = Number(maxMemberInput.val());

        // ===== 유효성 체크 =====
        if (!performanceId || !title || !content || !maxMember) {
            alert('모든 입력값을 올바르게 입력해주세요.');
            return;
        }

        if (!title.replace(/\s/g, '') || title.length < 2 || title.length > 100) {
            alert('제목은 2~100자 사이로 입력해주세요.');
            return;
        }

        if (!Number.isInteger(maxMember) || maxMember < 2 || maxMember > 50) {
            alert('모집 인원은 2~50 사이의 정수로 입력해주세요.');
            return;
        }

        const data = {
            performanceId: Number(performanceId),
            title,
            content,
            maxMember
        };

        console.log('전송 데이터:', data);

        // ===== Ajax 요청 =====
        $.ajax({
            url: `/api/performances/${performanceId}/chats`,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: (res) => {
                if (res.data) {
                    alert('채팅방이 생성되었습니다.');
                    window.location.href = `/performances/${performanceId}/chats`;
                } else {
                    alert(`실패: ${res.message || '알 수 없는 이유'}`);
                }
            },
            error: (xhr) => {
                console.error('채팅방 생성 실패:', xhr);
                const errorMsg = xhr.responseJSON?.message || xhr.responseText || '알 수 없는 에러가 발생했습니다.';

                switch (xhr.status) {
                    case 400:
                        alert(`입력값이 올바르지 않습니다:\n${errorMsg}`);
                        break;
                    case 401:
                        alert('로그인이 필요합니다.');
                        window.location.href = '/login';
                        break;
                    case 403:
                        alert('권한이 없습니다.');
                        break;
                    default:
                        alert(`에러 발생: ${errorMsg}`);
                }
            }
        });
    });
});
