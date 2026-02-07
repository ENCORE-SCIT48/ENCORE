$(document).ready(function() {

    const performanceId = $("#chat-list-container").data("performance-id");

    // 전체보기 + 버튼 클릭 이벤트
    $('#btn-show-all-joined').on('click', function() {
        location.href = '/chat/list/join'; // 전체 목록 페이지 URL
    });

    // =========================
    // 참여중 채팅방 3개 가져오기
    // =========================
    $.ajax({
        url: `/api/chat/join`,
        method: "GET",
        data: { limit: 3 },
        success: function(response) {

            let data = Array.isArray(response.data) ? response.data : [];

            if (data.length === 0) {
                $("#joined-chat-container").append(
                    `<div class="chat-empty">참여중인 채팅방이 없습니다.</div>`
                );
            } else {
                data.forEach(function(chat) {
                    const formattedDate = dayjs(chat.updatedAt).format('YY.MM.DD HH:mm');
                    $("#joined-chat-container").append(
                        `<div class="chat-item" onclick="location.href='/performance/${chat.performanceId}/chat/${chat.id}'">
                            <div class="performance-title">${chat.performanceTitle}</div>
                            <div class="chat-title">${chat.title}</div>
                            <div class="chat-info">
                                <span>${chat.currentMember}/${chat.maxMember}명 참여</span>
                                <span>${formattedDate}</span>
                            </div>
                         </div>`
                    );
                });
            }

            // 참여중 채팅방 없거나 로그인 안 됐으면 HOT 채팅방도 같이 로드
            loadHotChats();
        },
        error: function(err) {
            console.error("참여중 채팅방 로딩 실패", err);
            // 오류 시에도 HOT 채팅방 로드
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
            success: function(response) {
                let data = Array.isArray(response.data) ? response.data : [];

                if (data.length === 0) {
                    $("#chat-list-container").append(
                        `<div class="chat-empty">HOT 채팅방이 없습니다.</div>`
                    );
                    return;
                }

                data.forEach(function(chat) {
                    const formattedDate = dayjs(chat.updatedAt).format('YY.MM.DD HH:mm');

                    $("#chat-list-container").append(
                        `<div class="chat-item hot" onclick="location.href='/performance/${chat.performanceId}/chat/${chat.id}'">
                            <div class="performance-title">${chat.performanceTitle}</div>
                            <div class="chat-title">${chat.title}</div>
                            <div class="chat-info">
                                <span>${chat.currentMember}/${chat.maxMember}명 참여</span>
                                <span>${formattedDate}</span>
                            </div>
                         </div>`
                    );
                });
            },
            error: function(err) {
                console.error("HOT 채팅방 로딩 실패", err);
            }
        });
    }

});
