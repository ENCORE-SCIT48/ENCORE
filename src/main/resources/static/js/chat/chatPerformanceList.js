/**
 * chatPerformanceList.js
 * 🎯 공연 채팅 리스트 페이지 JS
 * - 공연별 채팅방 목록 조회
 * - 검색, 스크롤 무한 로딩 지원
 */

let currentPage = 0;
let isLoading = false;
let isLastPage = false;

$(document).ready(() => {
    const performanceId = $('#chat-list-container').data('performance-id');

    if (!performanceId) {
        console.error('performanceId가 설정되지 않았습니다.');
        return;
    }

    // 초기 채팅방 목록 로드
    loadChatList(0, performanceId);

    // 검색 버튼 클릭
    $('#searchBtn').click(() => resetAndSearch(performanceId));

    // 엔터 입력 시 검색
    $('#keyword').on('keypress', e => {
        if (e.key === 'Enter') resetAndSearch(performanceId);
    });

    // 마감 제외 체크박스 변경 시 검색
    $('#excludeClosed').change(() => resetAndSearch(performanceId));

    // 무한 스크롤
    $(window).scroll(() => {
        if ($(window).scrollTop() + $(window).height() > $(document).height() - 100) {
            if (!isLoading && !isLastPage) loadChatList(currentPage + 1, performanceId);
        }
    });
});

/**
 * 검색 초기화 후 채팅방 목록 로드
 * @param {number} performanceId 공연 ID
 */
function resetAndSearch(performanceId) {
    $('#chat-list-container').empty();
    currentPage = 0;
    isLastPage = false;
    loadChatList(0, performanceId);
}

/**
 * 채팅방 목록 조회
 * @param {number} page 페이지 번호
 * @param {number} performanceId 공연 ID
 */
function loadChatList(page, performanceId) {
    isLoading = true;

    $.ajax({
        url: `/api/performance/${performanceId}/list`,
        method: 'GET',
        data: {
            page: page,
            searchType: $('#searchType').val(),
            keyword: $('#keyword').val().trim(),
            onlyOpen: $('#excludeClosed').is(':checked')
        },
        success: res => {
            const slice = res.data;

            if (!slice || !slice.content || slice.content.length === 0) {
                isLastPage = true;
                if (page === 0) {
                    $('#chat-list-container').html(
                        '<p class="text-center mt-5">검색 결과가 없습니다.</p>'
                    );
                }
            } else {
                renderChatList(slice.content, performanceId);
                currentPage = page;
                isLastPage = slice.last;
            }
        },
        error: err => {
            console.error('채팅방 목록 로드 실패:', err);
            $('#chat-list-container').html(
                '<p class="text-center mt-5 text-danger">채팅방을 불러오는 데 실패했습니다.</p>'
            );
        },
        complete: () => {
            isLoading = false;
        }
    });
}

/**
 * 채팅방 아이템 렌더링
 * @param {Array} items 채팅방 데이터 배열
 * @param {number} performanceId 공연 ID
 */
function renderChatList(items, performanceId) {
    items.forEach(chat => {
        $('#chat-list-container').append(renderChatItem(chat, performanceId));
    });
}

/**
 * 단일 채팅방 아이템 HTML 생성
 * @param {Object} chat 채팅방 정보
 * @param {number} performanceId 공연 ID
 * @returns {string} HTML 문자열
 */
function renderChatItem(chat, performanceId) {
    const isClosed = chat.status === 'CLOSED';
    const statusClass = isClosed ? 'closed' : 'open';
    const statusText = isClosed ? '마감' : '모집중';
    const updatedAt = dayjs(chat.updatedAt).format('YY.MM.DD HH:mm');

    return `
        <div class="chat-item" onclick="location.href='/performance/${performanceId}/chat/${chat.id}'">
            <span class="status-badge ${statusClass}">${statusText}</span>
            <div class="chat-title">${chat.title}</div>
            <div class="chat-bottom">
                <span class="date">${updatedAt}</span>
                <span><i class="fa-solid fa-users"></i> ${chat.currentMember}/${chat.maxMember}명</span>
            </div>
        </div>
    `;
}
