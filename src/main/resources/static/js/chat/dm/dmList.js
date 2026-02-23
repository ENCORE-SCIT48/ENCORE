/**
 * dmList.js
 * 🎯 요청중인 DM + 참여중 DM 목록 관리
 */

$(document).ready(() => {
    // ========================
    // Constants & DOM Elements
    // ========================
    const $pendingContainer = $("#pending-dm-container");
    const $acceptedContainer = $("#accepted-dm-container");
    const $btnShowAllJoined = $("#btn-show-all-joined");

    // 전체 참여중 채팅방 페이지 이동
    $btnShowAllJoined.on('click', () => {
        window.location.href = '/chat/list/join';
    });

    // ========================
    // 함수 정의
    // ========================

    /**
     * 수락 요청 중인 DM 가져오기
     */
    const pendingDmRequests = () => {
        $.ajax({
            url: `/api/dms/pending`,
            method: "GET",
            success: (res) => {
                const chats = Array.isArray(res.data) ? res.data : [];

                if (chats.length === 0) {
                    $pendingContainer.append('<div class="chat-empty">요청 온 DM이 없습니다.</div>');
                } else {
                    chats.forEach((dm) => appendDmItem($pendingContainer, dm));
                }

                // 요청 중인 DM 로드 후 참여 중인 DM 로드
                acceptedDmRooms();
            },
            error: () => {
                alert("요청 중인 DM 로딩 실패");
                $pendingContainer.append('<div class="chat-empty">DM 로딩 실패</div>');
                acceptedDmRooms();
            }
        });
    };

    /**
     * 참여 중인 DM 가져오기
     */
    const acceptedDmRooms = () => {
        $.ajax({
            url: `/api/dms/accepted`,
            method: "GET",
            success: (res) => {
                const dms = Array.isArray(res.data) ? res.data : [];

                if (dms.length === 0) {
                    $acceptedContainer.append('<div class="chat-empty">참여 중인 DM이 없습니다.</div>');
                } else {
                    dms.forEach((dm) => appendDmItem($acceptedContainer, dm));
                }
            },
            error: () => {
                alert("참여 중인 DM 로딩 실패");
            }
        });
    };

    /**
     * DM DOM 생성 및 append
     * @param {jQuery} $container - append할 컨테이너
     * @param {Object} chat - 채팅방 데이터
     */
    const appendDmItem = ($container, chat) => {
        const formattedDate = chat.latestMessageAt
            ? dayjs(chat.latestMessageAt).format('YY.MM.DD HH:mm')
            : '';
        const unreadCount = chat.unreadCount > 0
            ? `<span class="unread-badge">${chat.unreadCount}</span>`
            : '';
        const profileImg = chat.otherUserProfileImg || '/default-profile.png'; // 기본 이미지

        const $dmItem = $(`
<div class="dm-item">
    <img class="profile-img" src="${profileImg}" alt="${chat.otherUserNickname} 프로필">
    <div class="dm-content">
        <div class="dm-header">
            <span class="nickname">${chat.otherUserNickname}</span>
            <span class="date">${formattedDate}</span>
            ${unreadCount}
        </div>
        <div class="dm-preview">${chat.latestMessage || '메시지가 없습니다.'}</div>
    </div>
</div>
        `);

        $dmItem.on('click', () => {
            window.location.href = `/dm/${chat.roomId}`;
        });

        $container.append($dmItem);
    };

    // ========================
    // 초기 로드
    // ========================
    pendingDmRequests();
});
