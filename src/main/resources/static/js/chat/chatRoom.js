/**
 * chatRoom.js
 * 🎯 채팅 메시지 전송/조회 및 참여자 목록 확인, 퇴장 기능 관리
 */

$(document).ready(() => {
    const chatInput = $('#chatInput');
    const chatArea = $('#chatArea');
    const participantList = $('#participantList');

    // 페이징 관련 변수
    let currentPage = 0;
    const pageSize = 20;
    let loading = false;
    let allLoaded = false;

    // 1. 초기 메시지 로드
    loadMessages(true);

    // 2. 사이드바 오픈 시 참여자 목록 로드
    $('#chatSidebar').on('show.bs.offcanvas', function () {
        fetchParticipants();
    });

    // 3. 메시지 전송 (클릭)
    $('#sendBtn').on('click', function () {
        sendMessage();
    });

    // 4. 메시지 전송 (엔터키)
    chatInput.on('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            sendMessage();
        }
    });

    // 5. 채팅방 퇴장
    $('#leaveChatBtn').on('click', function () {
        if (confirm('정말 채팅방에서 나가시겠습니까?')) {
            exitChat();
        }
    });

    /**
     * 메시지 전송 API 호출
     */
    function sendMessage() {
        const content = chatInput.val().trim();
        if (!content) return;

        $.ajax({
            url: `/api/chat/room/${ROOM_ID}/messages`,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ content }),
            success: function () {
                chatInput.val('');
                loadMessages(true); // 메시지 전송 후 최신 메시지 불러오기
            },
            error: function (xhr) {
                console.error('메시지 전송 실패:', xhr);
                alert('메시지를 보낼 수 없습니다.');
            }
        });
    }

    /**
     * 메시지 목록 불러오기 및 렌더링
     * @param reset true면 기존 메시지 초기화 후 새로 불러오기
     */
    function loadMessages(reset = false) {
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
            success: function (res) {
                if (!res.content || res.content.length === 0) {
                    allLoaded = true;
                    return;
                }

                res.content.forEach(function (msg) {
                    const html = `
                        <div class="message-row mb-3">
                            <div class="message-wrapper">
                                <div class="message-name">${msg.senderName}</div>
                                <div class="message-content">${msg.content}</div>
                            </div>
                        </div>
                    `;
                    chatArea.append(html);
                });

                scrollToBottom();
                currentPage++;
            },
            error: function (xhr) {
                console.error('메시지 로드 실패:', xhr);
            },
            complete: function () {
                loading = false;
            }
        });
    }

    /**
     * 참여자 목록 조회
     */
    function fetchParticipants() {
        $.ajax({
            url: `/api/chat/room/${ROOM_ID}/participants`,
            method: 'GET',
            success: function (res) {
                participantList.empty();

                 res.data.forEach(function (user) {
                                // li 태그 생성
                                const listItem = $(`
                                    <li class="list-group-item d-flex align-items-center" style="cursor:pointer">
                                        <i class="fa-solid fa-user-circle me-2 text-secondary"></i>
                                        ${user.nickName}
                                    </li>
                                `);

                                // 클릭 시 개인페이지로 이동
                                listItem.on('click', function () {
                                    const profileUrl = `/member/profile/${user.profileId}/${user.profileMode}`;
                                    window.location.href = profileUrl;
                                });

                                participantList.append(listItem);
                            });
                        },
                        error: function (xhr) {
                            console.error('참여자 목록 로드 실패:', xhr);
                        }
                    });


    /**
     * 채팅방 스크롤 최하단으로 이동
     */
    function scrollToBottom() {
        chatArea.scrollTop(chatArea[0].scrollHeight);
    }

    /**
     * 채팅방 퇴장 API 호출 및 목록 페이지 이동
     */
    function exitChat() {
        $.ajax({
            url: `/api/chat/room/${ROOM_ID}/exit`,
            method: 'POST',
            success: function (response) {
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
            error: function () {
                alert('퇴장 처리 중 오류가 발생했습니다.');
            }
        });
    }

    /**
     * 무한 스크롤 (위로 올릴 때 이전 메시지 로딩)
     */
    chatArea.on('scroll', function () {
        if (chatArea.scrollTop() === 0 && !loading && !allLoaded) {
            loadMessages();
        }
    });
});
