/**
 * 채팅방 정보 수정을 위한 제출 프로세스를 관리합니다.
 * - Hidden 필드에서 ID를 추출하고, 변경된 데이터를 JSON 형식으로 서버에 전송합니다.
 */
$(document).ready(function() {

    /** 수정 폼 제출 이벤트 핸들러 */
    $('#chatUpdateForm').submit(function(e) {
        e.preventDefault();

        // 1. HTML Hidden Input 및 입력 필드에서 데이터 추출
        const performanceId = $('#perfId').val();
        const id = $('#chatId').val();

        const data = {
            title: $('#title').val(),
            content: $('#content').val(),
            status: $('#status').val() // 모집 상태 (OPEN/CLOSED)
        };

        console.log("전송 데이터:", data);

        /** 채팅방 수정 API 호출 (AJAX) */
        $.ajax({
            url: `/performance/${performanceId}/chat/${id}/update`,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function(res) {
                // 서버 응답 규격(CommonResponse)에 따른 분기 처리
                if(res.data || res.success) {
                    alert('수정이 완료되었습니다.');
                    // 수정 완료 후 상세 페이지로 리다이렉트
                    window.location.href = `/performance/${performanceId}/chat/${id}`;
                } else {
                    alert('실패: ' + res.message);
                }
            },
            error: function(xhr) {
                console.error("에러 상세:", xhr);
                // 서버에서 전달한 에러 메시지가 있을 경우 우선 출력
                const errorMsg = xhr.responseJSON ? xhr.responseJSON.message : "서버 에러가 발생했습니다.";
                alert('에러 발생: ' + errorMsg);
            }
        });
    });

    /** 뒤로가기 버튼 클릭 시 이전 페이지로 이동 */
    $('#backBtn').click(function() {
        window.history.back();
    });
});
