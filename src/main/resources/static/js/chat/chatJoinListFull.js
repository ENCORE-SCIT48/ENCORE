/**
 * chatJoinListFull.js
 * 🎯 참여중 채팅방 리스트 조회 및 페이징
 */

$(document).ready(() => {

    let page = 0;
    const size = 20;
    let loading = false;
    let hasNext = true;

    const $container = $("#joined-chat-container");
    const $searchBtn = $("#searchBtn");
    const $keyword = $("#keyword");
    const $searchType = $("#searchType");

    /** 채팅방 조회 함수 */
    const loadChats = (reset = false) => {
        if (loading || (!reset && !hasNext)) return;

        loading = true;

        if (reset) {
            page = 0;
            hasNext = true;
            $container.empty();
        }

        $.ajax({
            url: "/api/users/{userId}/chats",
            method: "GET",
            data: {
                page,
                size,
                keyword: $keyword.val().trim(),
                searchType: $searchType.val()
            },
            success: res => {
                const slice = res.data;
                if (!slice) return;

                const chats = slice.content || [];
                hasNext = slice.hasNext;

                if (chats.length === 0 && reset) {
                    $container.append('<div class="no-result">검색 결과가 없습니다.</div>');
                }

                chats.forEach(chat => {
                    const updatedAt = dayjs(chat.updatedAt).format("YY.MM.DD HH:mm");
                    const isClosed = chat.status === 'CLOSED';
                    const statusClass = isClosed ? 'closed' : 'open';
                    const statusText = isClosed ? '마감' : '모집중';

                    const $chatItem = $(`
                        <div class="chat-item">
                            <span class="status-badge ${statusClass}">${statusText}</span>
                            <small class="performance-tag">${chat.performanceTitle || '공연 정보 없음'}</small>
                            <div class="chat-title">${chat.title}</div>
                            <div class="chat-info">
                                <span>${updatedAt}</span>
                                <span><i class="fa-solid fa-users"></i> ${chat.currentMember}/${chat.maxMember}명</span>
                            </div>
                        </div>
                    `);

                    // 클릭 이벤트 바인딩 (onclick 제거)
                    $chatItem.on('click', () => {
                        window.location.href = `/performance/${chat.performanceId}/chat/${chat.id}`;
                    });

                    $container.append($chatItem);
                });

                page++;
            },
            error: xhr => {
                console.error("채팅방 조회 실패", xhr);
                if (reset) {
                    $container.append('<div class="text-danger text-center mt-3">채팅방을 불러오는데 실패했습니다.</div>');
                }
            },
            complete: () => { loading = false; }
        });
    };

    /** 검색 초기화 및 호출 */
    const resetAndSearch = () => loadChats(true);

    // 초기 로드
    loadChats(true);

    // 검색 버튼 클릭
    $searchBtn.off("click").on("click", e => {
        e.preventDefault();
        resetAndSearch();
    });

    // Enter 키 검색
    $keyword.on("keydown", e => {
        if (e.key === "Enter") {
            e.preventDefault();
            resetAndSearch();
        }
    });

    // 무한 스크롤
    $(window).on("scroll", () => {
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 100) {
            loadChats();
        }
    });

});
