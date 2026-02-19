/**
 * chatJoinList.js
 * 🎯 참여중 채팅방 3개 + HOT 채팅방 10개 미리보기
 */

$(document).ready(() => {

    const performanceId = $("#chat-list-container").data("performance-id");
    const $joinedContainer = $("#joined-chat-container");
    const $hotContainer = $("#chat-list-container");
    const JOIN_LIMIT = 3;
    const HOT_LIMIT = 10;

    // 전체 참여중 채팅방 페이지 이동
    $("#btn-show-all-joined").on('click', () => {
        window.location.href = '/chats/join';
    });

    /** 참여중 채팅방 3개 가져오기 */
    const loadJoinedChats = () => {
        $.ajax({
            url: `/api/chat/join`,
            method: "GET",
            data: { limit: JOIN_LIMIT },
            success: res => {
                const chats = Array.isArray(res.data) ? res.data : [];

                if (chats.length === 0) {
                    $joinedContainer.append('<div class="chat-empty">참여중인 채팅방이 없습니다.</div>');
                } else {
                    chats.forEach(chat => {
                        appendChatItem($joinedContainer, chat);
                    });
                }

                loadHotChats(); // 참여중 로드 후 HOT 로드
            },
            error: err => {
                console.error("참여중 채팅방 로딩 실패", err);
                loadHotChats(); // 참여중 실패 시에도 HOT 로드
            }
        });
    };

    /** HOT 채팅방 10개 가져오기 */
    const loadHotChats = () => {
        $.ajax({
            url: `/api/chat/hot`,
            method: "GET",
            data: { limit: HOT_LIMIT },
            success: res => {
                const chats = Array.isArray(res.data) ? res.data : [];

                if (chats.length === 0) {
                    $hotContainer.append('<div class="chat-empty">HOT 채팅방이 없습니다.</div>');
                    return;
                }

                chats.forEach(chat => {
                    appendChatItem($hotContainer, chat, true);
                });
            },
            error: err => console.error("HOT 채팅방 로딩 실패", err)
        });
    };

    /**
     * 채팅방 DOM 생성 및 append
     * @param {jQuery} $container - append할 컨테이너
     * @param {Object} chat - 채팅방 데이터
     * @param {boolean} isHot - HOT 채팅방 여부
     */
    const appendChatItem = ($container, chat, isHot = false) => {
        const formattedDate = dayjs(chat.updatedAt).format('YY.MM.DD HH:mm');
        const isClosed = chat.status === 'CLOSED';
        const statusClass = isClosed ? 'closed' : 'open';
        const statusText = isClosed ? '마감' : '모집중';

        const $chatItem = $(`
            <div class="chat-item ${isHot ? 'hot' : ''}">
                <span class="status-badge ${statusClass}">${statusText}</span>
                <small class="performance-tag">${chat.performanceTitle || '공연 정보 없음'}</small>
                <div class="chat-title">${chat.title}</div>
                <div class="chat-info">
                    <span>${formattedDate}</span>
                    <span><i class="fa-solid fa-users"></i> ${chat.currentMember}/${chat.maxMember}명</span>
                </div>
            </div>
        `);

        $chatItem.on('click', () => {
            window.location.href = `/performance/${chat.performanceId}/chat/${chat.id}`;
        });

        $container.append($chatItem);
    };

    // 초기 로드
    loadJoinedChats();

});
