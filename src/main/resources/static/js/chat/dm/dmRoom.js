$(document).ready(function () {
    const participantStatus = $('#participantStatus').val();  // 예: 'PENDING', 'WAITING', 'ACCEPTED'

    if (participantStatus === 'PENDING') {
        // `PENDING` 상태에서는 수신자가 수락/거절을 해야하므로 입력창을 숨기고 수락/거절 버튼만 보이게
        $('#chatInput').hide();
        $('#sendBtn').hide();
        $('#acceptBtn').show();
        $('#rejectBtn').show();
    } else if (participantStatus === 'WAITING') {
        // `WAITING` 상태에서는 송신자만 채팅 입력 가능
        $('#chatInput').show();
        $('#sendBtn').show();
        $('#acceptBtn').hide();
        $('#rejectBtn').hide();
    } else if (participantStatus === 'ACCEPTED') {
        // `ACCEPTED` 상태에서는 양쪽 모두 채팅 입력 가능
        $('#chatInput').show();
        $('#sendBtn').show();
        $('#acceptBtn').hide();
        $('#rejectBtn').hide();
    }
    // 전송 버튼 클릭 시
    $('#sendBtn').on('click', function () {
        const content = $('#chatInput').val();

        if (!content) {
            return;  // 메시지가 비어있으면 전송하지 않음
        }

        $.ajax({
            url: '/api/dm/sendMessage',  // 메시지 전송 API
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                roomId: roomId,
                content: messageContent
            }),
            success: function (res) {
                alert('메시지가 전송되었습니다!');
                $('#chatInput').val('');  // 메시지 전송 후 입력창 비우기
            },
            error: function (xhr) {
                alert('메시지 전송 실패: ' + xhr.statusText);
            }
        });
    });
});
