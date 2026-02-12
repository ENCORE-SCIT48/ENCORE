/**
 * chatPostDetail.js
 * 🎯 채팅방 상세 페이지 JS
 * - 게시글 수정/삭제
 * - 채팅방 참가
 */

$(document).ready(() => {

    // =========================
    // 수정 버튼 클릭
    // =========================
    $('#btn-edit').on('click', function () {
        const postId = $(this).data('id');
        const performanceId = $(this).data('performance-id');

        if (!postId || !performanceId) {
            alert('게시글 ID 또는 공연 ID를 가져오지 못했습니다.');
            return;
        }

        // 수정 페이지로 이동
        window.location.href = `/performances/${performanceId}/chats/${postId}/edit`;
    });

    // =========================
    // 삭제 버튼 클릭
    // =========================
    $('#btn-delete').on('click', function () {
        const postId = $(this).data('id');
        const performanceId = $(this).data('performance-id'); // data 속성 이름 통일

        if (!postId || !performanceId) {
            alert('게시글 ID 또는 공연 ID를 가져오지 못했습니다.');
            return;
        }

        if (!confirm('정말 삭제하시겠습니까?')) return;

        $.ajax({
            url: `/api/chat/${postId}`,
            type: 'DELETE',
        success: () => {
            alert('삭제되었습니다.');
            window.location.href = `/performances/${performanceId}/chats`;
        },
            error: xhr => {

                const message = xhr.responseJSON?.message || xhr.responseText || '삭제 실패';
                alert(`삭제 실패: ${message}`);
            }
        });
    });

    // =========================
    // 채팅방 참가 버튼 클릭
    // =========================
    const $btnJoin = $('#btn-join');
    if ($btnJoin.length) {
        $btnJoin.on('click', function () {
            const roomId = $(this).data('room-id');

            if (!roomId) {
                alert('채팅방 ID를 가져오지 못했습니다.');
                return;
            }

            if (confirm('채팅방에 입장하시겠습니까?')) {
                window.location.href = `/chat/${roomId}`;
            }
        });
    }
});
