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
    console.log("렌더링 시도 중인 메시지:", msg); // <-- 이거 확인!
        // 1. 서버에서 준 mine 값이 있으면 쓰고, 없으면 현재 유저 ID와 비교 (안전장치)
        const isMine = msg.mine || (msg.profileId === CURRENT_PROFILE_ID && msg.profileMode === CURRENT_MODE);
        // 중복 렌더링 방지 (이미 화면에 있는 메시지 ID인지 확인)
        if ($(`.chat-message[data-id="${msg.messageId}"]`).length > 0) return;

        const html = `
            <div class="chat-message ${isMine ? 'mine' : 'other'}" data-id="${msg.messageId}">
                <span class="message-sender">${escapeHtml(msg.senderName)}</span>
                <div class="message-wrapper">
                    <span class="message-content">${escapeHtml(msg.content)}</span>
                </div>
                <span class="message-time">${dayjs(msg.createdAt).format('HH:mm')}</span>
            </div>`;

        // 로드 방식에 따라 위로 붙일지 아래로 붙일지 결정
        // 초기 로딩이나 페이징은 prepend가 필요할 수 있지만, 실시간은 append입니다.
        $chatContainer.append(html);
    }

    // ----------------------
    // WebSocket 연결
    // ----------------------
        const socket = new SockJS('/ws');
        const stompClient = Stomp.over(socket);

        // 디버그 로그가 너무 많으면 아래 주석 해제
        // stompClient.debug = null;

        stompClient.connect({}, function(frame) {
            console.log('소켓 연결 성공: ' + frame);

            // 구독 경로를 변수에 담아 로그 출력
            const subscribePath = '/user/queue/dm/' + ROOM_ID_VALUE;
            console.log('구독 시도 경로: ' + subscribePath);

            stompClient.subscribe(subscribePath, function(message) {
                console.log('!!! 실시간 메시지 수신됨 !!!'); // <-- 이게 뜨는지 보세요
                console.log('수신 데이터:', message.body);

                try {
                    const res = JSON.parse(message.body);
                    renderMessage(res);
                    scrollToBottom();
                } catch (e) {
                    console.error("메시지 처리 중 에러 발생:", e);
                }
            });
        }, function(error) {
            console.error('STOMP 연결 에러:', error);
        });
    // ========================
    // 메시지 전송
    // ========================
    function sendMessage() {
        const $input = $('#chatInput');
            const content = $input.val().trim();
            if (!content || !stompClient.connected) return; // 연결 확인 추가

            // 서버의 @MessageMapping("/dm/{roomId}")와 매칭
            stompClient.send('/app/dm/' + ROOM_ID_VALUE, {}, JSON.stringify({
                roomId: ROOM_ID_VALUE,
                content: content
            }));

            $input.val(''); // 입력창 비우기
            $input.focus();
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
