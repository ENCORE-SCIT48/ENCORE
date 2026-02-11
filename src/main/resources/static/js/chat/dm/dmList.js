/**
 * dmList.js
 * 🎯 요청중인 DM + 참여중이 DM
 */

$(document).ready(() => {

    const $pendingContainer = $("#pending-dm-container");
    const $acceptedContainer = $("#accepted-dm-container");

    // 전체 참여중 채팅방 페이지 이동
    $("#btn-show-all-joined").on('click', () => {
        window.location.href = '/chat/list/join';
    });

    /** 수락 요청 중인 dm 가져오기 */
    const pendingDmRequests = () => {
        $.ajax({
            url: `/api/dm/pending`,
            method: "GET",
            success: res => {
                const chats = Array.isArray(res.data) ? res.data : [];

                if (chats.length === 0) {
                    $pendingContainer.append('<div class="chat-empty">요청 온 DM이 없습니다.</div>');
                } else {
                    chats.forEach(dm => {
                        appendChatItem($pendingContainer, dm); // 수정됨
                    });
                }

                acceptedDmRooms(); // 요청 중인 채팅방 로드 후 참여 중인 채팅방 로드
            },
            error: err => {
                console.error("참여중 채팅방 로딩 실패", err);
                $pendingContainer.append('<div class="chat-empty">DM 로딩 실패</div>');
                acceptedDmRooms();
            }
        });
    };


    /** 참여중인 DM방 가져오기 */
    const acceptedDmRooms = () => {
        $.ajax({
            url: `/api/dm/accepted`,
            method: "GET",
            success: res => {
                const dm = Array.isArray(res.data) ? res.data : [];

                if (chats.length === 0) {
                    $acceptedContainer.append('<div class="chat-empty">참여 중인 DM이 없습니다.</div>');
                    return;
                }
            },
            error: err => console.error("참여 중인 dm방 로딩 실패", err)
        });
    };

    /**
     * DM DOM 생성 및 append
     * @param {jQuery} $container - append할 컨테이너
     * @param {Object} chat - 채팅방 데이터
     */
    const appendDmItem = ($container, chat) => {
        const formattedDate = dayjs(chat.latestMessageAt).format('YY.MM.DD HH:mm');
        const unreadCount = chat.unreadCount > 0 ? `<span class="unread-badge">${chat.unreadCount}</span>` : '';

        const $dmItem = $(`
            <div class="dm-item">
                <img class="profile-img" src="${chat.otherUser.profileImg}" alt="${chat.otherUser.nickname} 프로필">
                <div class="dm-content">
                    <div class="dm-header">
                        <span class="nickname">${chat.otherUser.nickname}</span>
                        <span class="date">${formattedDate}</span>
                        ${unreadCount}
                    </div>
                    <div class="dm-preview">${chat.latestMessage || '메시지가 없습니다.'}</div>
                </div>
            </div>
        `);

        $dmItem.on('click', () => {
            window.location.href = `/dm/${roomId}`;
        });

        $container.append($dmItem);
    };

    // 초기 로드
    pendingDmRequests();

});
