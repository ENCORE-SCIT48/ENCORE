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

    $('#btn-report').on('click', function () {
        const targetId = $(this).data('target-id');
        const targetType = $(this).data('target-type');
        const targetName = $(this).data('target-name');

        if (!targetId || !targetType || targetType === "undefined" || !targetName) {
            console.error("신고 데이터 누락:", { targetId, targetType, targetName });
            alert("신고 대상 정보가 올바르지 않아 신고를 진행할 수 없습니다.");
            return;
        }

        const url = `/report?targetId=${targetId}&targetType=${targetType}&targetName=${encodeURIComponent(targetName)}`;
        window.location.href = url;
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

            if (!confirm('채팅방에 입장하시겠습니까?')) return;

            // 서버 상태 확인
            $.ajax({
                url: `/api/chat/${roomId}/can-join`,
                method: 'GET',
                success: (res) => {
                    if (res.canJoin) {
                        window.location.href = `/chat/${roomId}`;
                    } else {
                        alert(res.message || '참여 불가 상태입니다.');
                    }
                },
                error: (xhr) => {
                    const msg = xhr.responseJSON?.message || '서버 오류 발생';
                    alert(msg);
                }
            });
        });
    }
});
