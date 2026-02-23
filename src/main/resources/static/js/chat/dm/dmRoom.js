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

    const participantStatus = $('#participantStatus').val();
    const ROOM_ID_VALUE = ROOM_ID; // 전역 ROOM_ID 사용
    const CURRENT_USER = CURRENT_USER_ID;

    // ========================
    // 초기 세팅
    // ========================
    loadMessages(true);
    setupParticipantUI();

    // ========================
    // UI 제어
    // ========================
    function setupParticipantUI() {
        if (participantStatus === 'PENDING') {
            $('#chatInput, #sendBtn').hide();
            $('#acceptBtn, #rejectBtn').show();
        } else {
            $('#chatInput, #sendBtn').show();
            $('#acceptBtn, #rejectBtn').hide();
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
        const isMine = msg.profileId === CURRENT_USER;

        const messageHtml = `
            <div class="chat-message ${isMine ? 'mine' : 'other'}">
                <div class="message-row mb-3">
                    <div class="message-wrapper">
                        <span class="message-sender">${msg.senderName}</span>
                        <span class="message-content">${msg.content}</span>
                        <span class="message-time">${dayjs(msg.createdAt).format('HH:mm')}</span>
                    </div>
                </div>
            </div>
        `;

        $chatContainer.append(messageHtml);
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
            data: JSON.stringify({ content }),
            success: (res) => {
                renderMessage(res.data);
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
    function handleParticipantStatus(type) {
        const isAccept = type === 'ACCEPTED';
        const method = isAccept ? 'PATCH' : 'DELETE';
        const actionText = isAccept ? '수락' : '거절';

        if (!confirm(`요청을 ${actionText}하시겠습니까?`)) return;

        $('#acceptBtn, #rejectBtn').prop('disabled', true);

        $.ajax({
            url: `/api/dms/${ROOM_ID_VALUE}`,
            method: method,
            success: () => {
                if (isAccept) {
                    alert('수락되었습니다.');
                    $('#acceptBtn, #rejectBtn').hide();
                    $('#chatInput, #sendBtn').fadeIn().css('display', 'flex');
                } else {
                    alert('거절되어 방이 삭제되었습니다.');
                    location.href = '/dm/list';
                }
            },
            error: () => {
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
