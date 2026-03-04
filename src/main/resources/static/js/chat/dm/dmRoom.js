/**
 * dmRoom.js
 * 🎯 1:1 DM 메시지 전송 및 조회, 스크롤 로딩 관리
 */

$(document).ready(() => {

    // ========================
    // Constants & State
    // ========================
    const $chatContainer = $("#chatArea");
    const pageSize = 20;

    let currentPage = 0;
    let hasMore = true;

    const participantStatus = PARTICIPANT_STATUS;
    const ROOM_ID_VALUE = ROOM_ID; // 전역 ROOM_ID 사용

    // ========================
    // 초기 세팅
    // ========================
    loadMessages(true);
    setupParticipantUI();

    // ========================
        // XSS 방지
        // ========================
        function escapeHtml(text) {
            return $('<div>').text(text).html();
        }

    // ========================
    // UI 제어
    // ========================
    function setupParticipantUI() {
        const status = PARTICIPANT_STATUS;

        if (status === 'ACCEPTED' || status === 'WAITING') {
            // 수락/거절 버튼 그룹을 강제로 숨김 (!important 효과)
            $('#actionBtnGroup').attr('style', 'display: none !important');

            // 입력창과 전송버튼 표시
            $('#chatInput').fadeIn().css('display', 'block');
            $('#sendBtn').fadeIn().css('display', 'inline-block');
        } else {
            // PENDING 상태일 때 (버튼 그룹은 flex로 보여줌)
            $('#actionBtnGroup').attr('style', 'display: flex !important');
            $('#chatInput, #sendBtn').hide();
        }
    }
    // ========================
    // 메시지 로드
    // ========================
    function loadMessages(reset = false) {
        if (!hasMore) return;

        if (reset) {
            currentPage = 0;
            hasMore = true;
            $chatContainer.empty();
        }

        $.ajax({
            url: `/api/dms/${ROOM_ID_VALUE}/messages`,
            method: 'GET',
            data: { page: currentPage, size: pageSize },
            success: (res) => {
                const messages = Array.isArray(res.data) ? res.data : [];

                if (messages.length === 0) {
                    hasMore = false;
                    return;
                }

                messages.forEach(renderMessage);

                currentPage++;
                scrollToBottom();
            },
            error: () => {
                alert("DM 메시지 로딩 중 오류가 발생했습니다.");
            }
        });
    }

    // ========================
    // 메시지 렌더링
    // ========================
    function renderMessage(msg) {
        const isMine = msg.mine; // 서버에서 채팅 메시지 DTO에 mine 포함 필요
        const html = `
            <div class="chat-message ${isMine ? 'mine' : 'other'}" data-id="${msg.messageId}">
                <span class="message-sender">${escapeHtml(msg.senderName)}</span>
                <div class="message-wrapper">
                    <span class="message-content">${escapeHtml(msg.content)}</span>
                </div>
                <span class="message-time">${dayjs(msg.createdAt).format('HH:mm')}</span>
            </div>`;
        $chatContainer.append(html);
    }

    // ========================
    // 메시지 전송
    // ========================
    function sendMessage() {
        const content = $('#chatInput').val().trim();
        if (!content) return;

        $.ajax({
            url: `/api/dms/${ROOM_ID_VALUE}/messages`,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ roomId: ROOM_ID_VALUE, content:content }),
            success: (res) => {
                const newMessage = res.data;
                newMessage.mine = true;

                renderMessage(newMessage);
                $('#chatInput').val('');
                scrollToBottom();
            },
            error: (xhr) => {
                alert('메시지 전송 실패: ' + xhr.statusText);
            }
        });
    }

    // ========================
    // 참여 상태 처리
    // ========================
    function handleParticipantStatus(type) { // type은 'ACCEPTED' 또는 'REJECTED'
        const isAccept = type === 'ACCEPTED';
        const actionText = isAccept ? '수락' : '거절';

        if (!confirm(`요청을 ${actionText}하시겠습니까?`)) return;

        $('#acceptBtn, #rejectBtn').prop('disabled', true);

        $.ajax({
            url: `/api/dms/${ROOM_ID_VALUE}`,
            method: 'PATCH', // 거절도 '상태 변경' 요청이므로 PATCH
            contentType: 'application/json',
            // 핵심: 여기서 type(REJECTED)을 status라는 키에 담아 보냅니다.
            data: JSON.stringify({ status: type }),
            success: (res) => {
                if (isAccept) {
                    alert('수락되었습니다.');
                    PARTICIPANT_STATUS = 'ACCEPTED';
                    setupParticipantUI();
                } else {
                    alert('거절되어 방이 삭제되었습니다.');
                    location.href = '/dm/list';
                }
            },
            error: (xhr) => {
                console.error("실패 사유:", xhr.responseText);
                alert('처리 중 오류가 발생했습니다.');
                $('#acceptBtn, #rejectBtn').prop('disabled', false);
            }
        });
    }

    // ========================
    // 유틸
    // ========================
    function scrollToBottom() {
        $chatContainer.scrollTop($chatContainer.prop("scrollHeight"));
    }

    // ========================
    // Event Bindings
    // ========================
    $('#sendBtn').on('click', sendMessage);

    $('#chatInput').on('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            sendMessage();
        }
    });

    $('#acceptBtn').on('click', () => handleParticipantStatus('ACCEPTED'));
    $('#rejectBtn').on('click', () => handleParticipantStatus('REJECTED'));

    $chatContainer.on('scroll', () => {
        if ($chatContainer.scrollTop() <= 10) {
            loadMessages();
        }
    });

});
