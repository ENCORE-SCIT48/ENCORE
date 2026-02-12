/**
 * chatRoom.js
 * 🎯 채팅 메시지 전송/조회 및 참여자 목록 확인, 퇴장 기능 관리
 */

$(document).ready(() => {
    // ========================
    // Constants & State
    // ========================
    const chatInput = $('#chatInput');
    const chatArea = $('#chatArea');
    const participantList = $('#participantList');
    const pageSize = 20;

    let currentPage = 0;
    let loading = false;
    let allLoaded = false;

    // ========================
    // 초기 메시지 로드
    // ========================
    loadMessages(true);

    // ========================
    // Event Bindings
    // ========================
    // 참여자 목록 로드 (사이드바 오픈 시)
    $('#chatSidebar').on('show.bs.offcanvas', () => fetchParticipants());

    // 메시지 전송 (클릭)
    $('#sendBtn').on('click', () => sendMessage());

    // 메시지 전송 (엔터키)
    chatInput.on('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            sendMessage();
        }
    });

    // 채팅방 퇴장
    $('#leaveChatBtn').on('click', () => {
        if (confirm('정말 채팅방에서 나가시겠습니까?')) {
            exitChat();
        }
    });

    // 무한 스크롤 (위로 올릴 때 이전 메시지 로딩)
    chatArea.on('scroll', () => {
        if (chatArea.scrollTop() <= 10 && !loading && !allLoaded) {
            loadMessages();
        }
    });

    // ========================
    // Functions
    // ========================

    /**
     * 메시지 전송
     */
    const sendMessage = () => {
        const content = chatInput.val().trim();
        if (!content) return;

        $.ajax({
            url: `/api/chat/room/${ROOM_ID}/messages`,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ content }),
            success: () => {
                chatInput.val('');
                loadMessages(true);
            },
            error: () => {
                alert('메시지를 보낼 수 없습니다.');
            }
        });
    };

    /**
     * 메시지 목록 불러오기 및 렌더링
     * @param {boolean} reset - true면 기존 메시지 초기화 후 새로 불러오기
     */
    const loadMessages = (reset = false) => {
        if (loading || allLoaded) return;
        loading = true;

        if (reset) {
            currentPage = 0;
            allLoaded = false;
            chatArea.empty();
        }

        $.ajax({
            url: `/api/chat/room/${ROOM_ID}/messages?page=${currentPage}&size=${pageSize}`,
            method: 'GET',
            success: (res) => {
                if (!res.content || res.content.length === 0) {
                    allLoaded = true;
                    return;
                }

                res.content.forEach((msg) => {
                    const html = `
<div class="chat-message ${msg.profileId === CURRENT_USER_ID ? 'mine' : 'other'}">
    <div class="message-row mb-3">
        <div class="message-wrapper">
            <span class="message-sender">${msg.senderName}</span>
            <span class="message-content">${msg.content}</span>
            <span class="message-time">${dayjs(msg.createdAt).format('HH:mm')}</span>
        </div>
    </div>
</div>`;
                    chatArea.append(html);
                });

                scrollToBottom();
                currentPage++;
            },
            error: () => {
                alert('메시지를 불러오는 중 오류가 발생했습니다.');
            },
            complete: () => {
                loading = false;
            }
        });
    };

    /**
     * 참여자 목록 조회
     */
    const fetchParticipants = () => {
        $.ajax({
            url: `/api/chat/room/${ROOM_ID}/participants`,
            method: 'GET',
            success: (res) => {
                participantList.empty();

                res.data.forEach((user) => {
                    const listItem = $(`
<li class="list-group-item d-flex align-items-center" style="cursor:pointer">
    <i class="fa-solid fa-user-circle me-2 text-secondary"></i>
    ${user.nickName}
</li>`);
                    listItem.on('click', () => {
                        window.location.href = `/member/profile/${user.profileId}/${user.profileMode}`;
                    });
                    participantList.append(listItem);
                });
            },
            error: () => {
                alert('참여자 목록을 불러오는 중 오류가 발생했습니다.');
            }
        });
    };

    /**
     * 채팅방 스크롤 최하단으로 이동
     */
    const scrollToBottom = () => {
        chatArea.scrollTop(chatArea[0].scrollHeight);
    };

    /**
     * 채팅방 퇴장
     */
    const exitChat = () => {
        $.ajax({
            url: `/api/chat/room/${ROOM_ID}/exit`,
            method: 'POST',
            success: (response) => {
                if (!response.exitSuccess) {
                    if (response.isOwner) {
                        alert('글쓴이는 퇴장할 수 없습니다.');
                    } else {
                        alert(response.message);
                    }
                    return;
                }
                alert(response.message);
                location.href = '/chat/list';
            },
            error: () => {
                alert('퇴장 처리 중 오류가 발생했습니다.');
            }
        });
    };
});
