/**
 * 신규 채팅방 등록을 위한 폼 제출 프로세스를 관리합니다.
 * - 입력 데이터를 JSON으로 변환하여 비동기(Ajax) 서버 요청을 수행합니다.
 */
$('#chatPostForm').submit(function(e) {
    e.preventDefault();

    const performanceId = $('#performanceId').val();

    const data = {
        performanceId: Number(performanceId),
        title: $('#title').val(),
        content: $('#content').val(),
        maxMember: Number($('#maxMember').val())
    };

    /** 채팅방 생성 API 호출 */
    $.ajax({
        url: '/performance/' + performanceId + '/chat/post',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function(res) {
            if(res.data) {
                alert('채팅방이 생성되었습니다.');
                window.location.href = '/performance/' + performanceId + '/chat/list';
            } else {
                alert('실패: ' + res.message);
            }
        },
        error: function(xhr) {
            console.error("에러 상세:", xhr);
            let errorMessage = "알 수 없는 에러가 발생했습니다.";

            if (xhr.responseJSON) {
                errorMessage = xhr.responseJSON.message;
            }
            if (xhr.status === 400) {
                alert("입력값이 올바르지 않습니다:\n" + errorMessage);
            } else if (xhr.status === 401) {
                alert("로그인이 필요합니다.");
                window.location.href = "/login";
            } else if (xhr.status === 403) {
                alert("권한이 없습니다.");
            } else {
                alert("에러 발생: " + errorMessage);
            }
        }
    });
});
