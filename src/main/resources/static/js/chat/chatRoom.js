/**
 * chatRoom.js
 * 🎯 페이지네이션 + 중복 방지 + isMine 필드 활용 버전
 */

$(document).ready(() => {

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
    // 이벤트 바인딩
    // ========================
    $('#chatSidebar').on('show.bs.offcanvas', fetchParticipants);
    $('#sendBtn').on('click', sendMessage);
    chatInput.on('keydown', (e) => { if (e.key === 'Enter') { e.preventDefault(); sendMessage(); }});
    $('#leaveChatBtn').on('click', () => { if (confirm('정말 채팅방에서 나가시겠습니까?')) exitChat(); });
    chatArea.on('scroll', () => {
        if (chatArea.scrollTop() <= 10 && !loading && !allLoaded) loadMessages();
    });

    // ========================
    // 메시지 전송
    // ========================
    function sendMessage() {
        const content = chatInput.val().trim();
        if (!content) return;

        $.ajax({
            url: `/api/chat/room/${ROOM_ID}/messages`,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ content }),
            success: (res) => {
                        if (!res.success) return alert(res.message);

                        chatInput.val('');

                        // 💡 방법 A: 전체를 다시 불러오지 않고, 서버가 준 응답값으로 내 메시지만 바로 추가
                        // res.data에 생성된 messageId, createdAt 등이 포함되어 있다고 가정합니다.
                        if (res.data) {
                            renderMessage(res.data);
                            scrollToBottom();
                        } else {
                            // 만약 서버 응답에 데이터가 없다면 기존처럼 새로고침 호출 (단, 위 조건 수정 필수)
                            loadMessages(true);
                        }
                    },
            error: handleAjaxError
        });
    }

    // ========================
    // 메시지 목록 조회
    // ========================
    function loadMessages(reset = false) {
        if (loading || allLoaded) return;
        loading = true;

        if (reset) {
            currentPage = 0;
            allLoaded = false;
        }

        $.ajax({
            url: `/api/chat/room/${ROOM_ID}/messages?page=${currentPage}&size=${pageSize}`,
            method: 'GET',
            success: (res) => {

                if (!res.success) { alert(res.message); return; }

                const messages = res.data;
                if (!messages || messages.length === 0) {
                    allLoaded = true;
                    if (reset) chatArea.html(`<div class="message-row system-msg"><p class="text-center text-muted small">대화가 시작되었습니다.</p></div>`);
                    return;
                }

                // reset이면 기존 메시지 초기화
                if (reset) chatArea.empty();

                // 중복 메시지 방지 후 append
                messages.forEach(msg => {
                    if (chatArea.find(`[data-id="${msg.messageId}"]`).length === 0) {
                        renderMessage(msg);
                    }
                });

                // 스크롤 자동 하단
                scrollToBottom();

                currentPage++;
            },
            error: handleAjaxError,
            complete: () => { loading = false; }
        });
    }

    // ========================
    // 메시지 렌더링
    // ========================
        function renderMessage(msg) {
            const isMine = msg.mine;
            const html = `
        <div class="chat-message ${isMine ? 'mine' : 'other'}" data-id="${msg.messageId}">
            <span class="message-sender">${escapeHtml(msg.senderName)}</span>
            <div class="message-wrapper">
                <span class="message-content">${escapeHtml(msg.content)}</span>
            </div>
            <span class="message-time">${dayjs(msg.createdAt).format('HH:mm')}</span>
        </div>`;
            chatArea.append(html);
        }
    // ========================
    // 참여자 목록 조회
    // ========================
    function fetchParticipants() {
        $.ajax({
            url: `/api/chat/room/${ROOM_ID}/participants`,
            method: 'GET',
            success: (res) => {
                if (!res.success) return alert(res.message);

                participantList.empty();
                res.data.forEach(user => {
                    const listItem = $(`
<li class="list-group-item d-flex align-items-center" style="cursor:pointer">
    <i class="fa-solid fa-user-circle me-2 text-secondary"></i>
    ${escapeHtml(user.nickName)}
</li>`);

                    listItem.on('click', () => {
                        window.location.href = `/member/profile/${user.profileId}/${user.profileMode}`;
                    });

                    participantList.append(listItem);
                });
            },
            error: handleAjaxError
        });
    }

    // ========================
    // 채팅방 퇴장
    // ========================
    function exitChat() {
        $.ajax({
            url: `/api/chat/room/${ROOM_ID}/exit`,
            method: 'POST',
            success: (res) => {
                if (!res.success) return alert(res.message);

                const data = res.data;
                if (!data.exitSuccess) {
                    if (data.isOwner) alert('글쓴이는 퇴장할 수 없습니다.');
                    else alert(res.message);
                    return;
                }

                alert(res.message);
                location.href = '/chat/list';
            },
            error: handleAjaxError
        });
    }

    // ========================
    // 공통 에러 처리
    // ========================
    function handleAjaxError(xhr) {
        console.error('AJAX Error:', xhr.responseText);
        alert('서버 오류가 발생했습니다.');
    }

    // ========================
    // XSS 방지
    // ========================
    function escapeHtml(text) {
        return $('<div>').text(text).html();
    }

    // ========================
    // 스크롤 하단 이동
    // ========================
function scrollToBottom() {
    setTimeout(() => {
        chatArea.scrollTop(chatArea[0].scrollHeight);
    }, 50); // 0.05초의 여유를 줌
}
});
