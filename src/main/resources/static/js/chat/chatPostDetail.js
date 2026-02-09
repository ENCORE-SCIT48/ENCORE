/**
 * chatPostDetail.js
 * 🎯 채팅방 상세 페이지 JS
 * - 게시글 수정 페이지 이동
 * - 게시글 삭제 요청
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

        window.location.href = `/performance/${performanceId}/chat/${postId}/update`;
    });

    // =========================
    // 삭제 버튼 클릭
    // =========================
    $('#btn-delete').on('click', function () {
        const postId = $(this).data('id');
        const performanceId = $(this).data('performanceId');

        if (!postId) {
            alert('게시글 ID를 가져오지 못했습니다.');
            return;
        }

        if (!confirm('정말 삭제하시겠습니까?')) return;

        $.ajax({
            url: `/chat/${postId}`,
            type: 'DELETE',
        success: () => {
            alert('삭제되었습니다.');
            window.location.href = `/performance/${performanceId}/chat/list`;
        },
            error: xhr => {
                const message = xhr.responseJSON?.message || xhr.responseText || '삭제 실패';
                alert(`삭제 실패: ${message}`);
            }
        });
    });
});
