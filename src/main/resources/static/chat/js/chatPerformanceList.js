/**
 * 채팅방 목록 페이지의 이벤트 핸들러를 관리합니다.
 * - 뒤로 가기, 상세 페이지 이동, 글쓰기 페이지 이동 기능을 포함합니다.
 */
$(function() {

    /** 이전 페이지 이동 */
    $('#backBtn').click(function() {
        window.history.back();
    });

    /** 채팅방 카드 클릭 시 상세 정보 페이지로 이동 */
    $('.chat-card').click(function() {
        // data-속성에서 공연 ID와 채팅방 ID 추출
        const performanceId = $(this).attr('data-performance-id');
        const chatId = $(this).data('id');

        if (performanceId && chatId) {
            window.location.href = `/performance/${performanceId}/chat/${chatId}`;
        }
    });

    /** 새 채팅방 작성 페이지로 이동 */
    $('#writeBtn').click(function() {
        const performanceId = $(this).data('performanceId');

        if (performanceId) {
            window.location.href = `/performance/${performanceId}/chat/post`;
        }
    });

});
