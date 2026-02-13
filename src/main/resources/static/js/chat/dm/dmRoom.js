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

    // ========================
    // 초기 메시지 로드
    // ========================
    loadMessages(true);

    // participantStatus에 따른 입력창/버튼 제어
    const participantStatus = $('#participantStatus').val(); // 'PENDING', 'WAITING', 'ACCEPTED'
    if (participantStatus === 'PENDING') {
        $('#chatInput, #sendBtn').hide();
        $('#acceptBtn, #rejectBtn').show();
    } else {
        $('#chatInput, #sendBtn').show();
        $('#acceptBtn, #rejectBtn').hide();
    }

    // ========================
    // 함수 정의
    // ========================

    /**
     * 메시지 불러오기
     * @param {boolean} reset - true면 초기화 후 불러오기
     */
    const loadMessages = (reset = false) => {
        if (!hasMore) return;

        if (reset) {
            currentPage = 0;
            hasMore = true;
            $chatContainer.empty();
        }

        $.ajax({
            url: `/api/dms/${ROOM_ID}/messages`,
            method: 'GET',
            data: { page: currentPage, size: pageSize },
            success: (res) => {
                const messages = Array.isArray(res.data) ? res.data : [];

                if (messages.length === 0) {
                    hasMore = false;
                    return;
                }

                messages.forEach((msg) => {
                    const messageHtml = `
                        <div class="chat-message ${msg.profileId === CURRENT_USER_ID ? 'mine' : 'other'}">
                            <div class="message-row mb-3">
                                <div class="message-wrapper">
                                    <span class="message-sender">${msg.senderName}</span>
                                    <span class="message-content">${msg.content}</span>
                                    <span class="message-time">${dayjs(msg.createdAt).format('HH:mm')}</span>
                                </div>
                            </div>
                        </div>`;
                    $chatContainer.append(messageHtml);
                });

                currentPage++;
                $chatContainer.scrollTop($chatContainer.prop("scrollHeight"));
            },
            error: () => {
                alert("DM 메시지 로딩 중 오류가 발생했습니다.");
            }
        });
    };

    /**
     * 메시지 전송
     */
    const sendMessage = () => {
        const content = $('#chatInput').val().trim();
        if (!content) return;

        $.ajax({
            url: `/api/dms/${ROOM_ID}/messages`,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ roomId: ROOM_ID, content }),
            success: (res) => {
                const msg = res.data;
                const messageHtml = `
<div class="chat-message mine">
    <div class="message-row mb-3">
        <div class="message-wrapper">
            <span class="message-sender">${msg.senderName}</span>
            <span class="message-content">${msg.content}</span>
            <span class="message-time">${dayjs(msg.createdAt).format('HH:mm')}</span>
        </div>
    </div>
</div>`;
                $chatContainer.append(messageHtml);
                $('#chatInput').val('');
                $chatContainer.scrollTop($chatContainer.prop("scrollHeight"));
            },
            error: (xhr) => {
                alert('메시지 전송 실패: ' + xhr.statusText);
            }
        });
    };

    // ========================
    // Event Bindings
    // ========================
    // 메시지 전송 (클릭)
    $('#sendBtn').on('click', sendMessage);

    // 메시지 전송 (엔터키)
    $('#chatInput').on('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            sendMessage();
        }
    });

// ========================
// [추가] 참여 수락/거절 및 논리 삭제 처리
// ========================
$('#acceptBtn, #rejectBtn').on('click', function() {
    const isAccept = $(this).attr('id') === 'acceptBtn';
    const newStatus = isAccept ? "ACCEPTED" : "REJECTED";
    const isDelete = !isAccept; // 거절일 경우 논리 삭제 수행

    if (!confirm(`정말 ${isAccept ? '수락' : '거절'}하시겠습니까?${isDelete ? '\n(거절 시 이 채팅방은 삭제됩니다.)' : ''}`)) {
        return;
    }

    // 버튼 비활성화 (중복 클릭 방지)
    const $btns = $('#acceptBtn, #rejectBtn').prop('disabled', true);

    // ========================
    // [최적화] 참여 수락(PATCH) / 거절(DELETE) 처리
    // ========================
  /**
   * [설명] 참여 상태를 변경하거나 방을 삭제합니다.
   * @param {string} type - 'ACCEPTED' 또는 'REJECTED'
   */
  const handleParticipantStatus = (type) => {
      const isAccept = type === 'ACCEPTED';
      const method = isAccept ? 'PATCH' : 'DELETE';
      const actionText = isAccept ? '수락' : '거절';

      if (!confirm(`요청을 ${actionText}하시겠습니까?`)) {
          return;
      }

      const $btns = $('#acceptBtn, #rejectBtn').prop('disabled', true);

      $.ajax({
          url: `/api/dms/${ROOM_ID}`,
          method: method,
          success: (res) => {
              if (isAccept) {
                  alert('수락되었습니다.');
                  $('#acceptBtn, #rejectBtn').hide();
                  $('#chatInput, #sendBtn').fadeIn().css('display', 'flex');
              } else {
                  alert('거절되어 방이 삭제되었습니다.');
                  location.href = '/performances';
              }
          },
          error: (xhr) => {
              console.error('[ERROR] 상태 변경 실패:', xhr);
              alert('처리 중 오류가 발생했습니다.');
              $btns.prop('disabled', false);
          },
      });
  };

  // Event Bindings
  $('#acceptBtn').on('click', () => handleParticipantStatus('ACCEPTED'));
  $('#rejectBtn').on('click', () => handleParticipantStatus('REJECTED'));

    // 스크롤 상단 시 이전 메시지 로딩
    $chatContainer.on('scroll', () => {
        if ($chatContainer.scrollTop() <= 10) {
            loadMessages();
        }
    });
});
