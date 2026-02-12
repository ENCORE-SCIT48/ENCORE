$(document).ready(function () {
    const participantStatus = $('#participantStatus').val();  // 'PENDING', 'WAITING', 'ACCEPTED'
    const $chatContainer = $("#chat-container");
    const pageSize = 20;
    let currentPage = 0;
    let hasMore = true;

    // participantStatus에 따라 입력창/버튼 제어
    if (participantStatus === 'PENDING') {
        $('#chatInput, #sendBtn').hide();
        $('#acceptBtn, #rejectBtn').show();
    } else if (participantStatus === 'WAITING') {
        $('#chatInput, #sendBtn').show();
        $('#acceptBtn, #rejectBtn').hide();
    } else if (participantStatus === 'ACCEPTED') {
        $('#chatInput, #sendBtn').show();
        $('#acceptBtn, #rejectBtn').hide();
    }

    // 메시지 불러오기 (페이징)
    const loadMessages = () => {
        if (!hasMore) return;

        $.ajax({
            url: `/api/chat/room/{roomId}/messages`,
            method: 'GET',
            data: { roomId: ROOM_ID, page: currentPage, size: pageSize },
            success: function (res) {
                const messages = Array.isArray(res.data) ? res.data : [];

                if (messages.length < pageSize) hasMore = false;

                messages.forEach(msg => {
                    const messageHtml = `
                        <div class="chat-message ${msg.profileId === CURRENT_USER_ID ? 'mine' : 'other'}">
                            <span class="sender">${msg.senderName}</span>
                            <span class="content">${msg.content}</span>
                            <span class="time">${dayjs(msg.createdAt).format('HH:mm')}</span>
                        </div>
                    `;
                    $chatContainer.prepend(messageHtml); // 최신 메시지 아래로
                });

                currentPage++;
            },
            error: function (xhr) {
                console.error("DM 메시지 로딩 실패:", xhr);
            }
        });
    };

    // 초기 로드
    loadMessages();

    // 스크롤 상단 시 이전 메시지 로딩
    $chatContainer.on('scroll', function () {
        if ($chatContainer.scrollTop() === 0) loadMessages();
    });

    // 메시지 전송
    $('#sendBtn').on('click', function () {
        const content = $('#chatInput').val();
        if (!content) return;

        $.ajax({
            url: '/api/dm/sendMessage',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ roomId: ROOM_ID, content: content }),
            success: function (res) {
                const msg = res.data;
                const messageHtml = `
                    <div class="chat-message mine">
                        <span class="sender">${msg.senderName}</span>
                        <span class="content">${msg.content}</span>
                        <span class="time">${dayjs(msg.createdAt).format('HH:mm')}</span>
                    </div>
                `;
                $chatContainer.append(messageHtml);
                $('#chatInput').val('');
                $chatContainer.scrollTop($chatContainer.prop("scrollHeight"));
            },
            error: function (xhr) {
                alert('메시지 전송 실패: ' + xhr.statusText);
            }
        });
    });
});
