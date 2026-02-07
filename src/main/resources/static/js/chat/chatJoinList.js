$(document).ready(function () {
    const performanceId = $("#chat-list-container").data("performance-id");
    $('#btn-show-all-joined').on('click', function () {
        location.href = '/chat/list/join'; // 전체 목록 페이지 URL
    });

    // =========================
    // 참여중 채팅방 3개 가져오기
    // =========================
    $.ajax({
        url: `/api/chat/join`,
        method: "GET",
        data: { limit: 3 },
        success: function (response) {

            let data = Array.isArray(response.data) ? response.data : [];

            if (data.length === 0) {
                $("#joined-chat-container").append(
                    `<div class="chat-empty">참여중인 채팅방이 없습니다.</div>`
                );
            } else {
                data.forEach(function (chat) {
                    const formattedDate = dayjs(chat.updatedAt).format('YY.MM.DD HH:mm');
                    const isClosed = chat.status === 'CLOSED';
                    const statusClass = isClosed ? 'closed' : 'open';
                    const statusText = isClosed ? '마감' : '모집중';

                    $("#joined-chat-container").append(
                        `<div class="chat-item" onclick="location.href='/performance/${chat.performanceId}/chat/${chat.id}'">
                            <span class="status-badge ${statusClass}">${statusText}</span>
                            <small class="performance-tag">${chat.performanceTitle || '공연 정보 없음'}</small>
                            <div class="chat-title">${chat.title}</div>
                        <div class="chat-info">
                                <span>${formattedDate}</span>
                                <span><i class="fa-solid fa-users"></i> ${chat.currentMember}/${chat.maxMember}명</span>
                        </div>
                        </div>`


                    );
                });
            }
            loadHotChats();
        },
        error: function (err) {
            console.error("참여중 채팅방 로딩 실패", err);
            loadHotChats();
        }
    });

    // =========================
    // HOT 채팅방 10개 가져오기
    // =========================
    function loadHotChats() {
        $.ajax({
            url: `/api/chat/hot`,
            method: "GET",
            data: { limit: 10 },
            success: function (response) {
                let data = Array.isArray(response.data) ? response.data : [];

                if (data.length === 0) {
                    $("#chat-list-container").append(
                        `<div class="chat-empty">HOT 채팅방이 없습니다.</div>`
                    );
                    return;
                }

                data.forEach(function (chat) {
                    const formattedDate = dayjs(chat.updatedAt).format('YY.MM.DD HH:mm');
                    const isClosed = chat.status === 'CLOSED';
                    const statusClass = isClosed ? 'closed' : 'open';
                    const statusText = isClosed ? '마감' : '모집중';

                    $("#chat-list-container").append(
                        `<div class="chat-item hot" onclick="location.href='/performance/${chat.performanceId}/chat/${chat.id}'">
<span class="status-badge ${statusClass}">${statusText}</span>
                            <small class="performance-tag">${chat.performanceTitle || '공연 정보 없음'}</small>

                            <div class="chat-title">${chat.title}</div>
                            <div class="chat-info">
                                <span>${formattedDate}</span>
                                <div class="member-count">${chat.currentMember}/${chat.maxMember}</div>
                            </div>
                        </div>`
                    );
                });
            },
            error: function (err) {
                console.error("HOT 채팅방 로딩 실패", err);
            }
        });
    }

});
