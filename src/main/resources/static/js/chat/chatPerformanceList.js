let currentPage = 0;
let isLoading = false;
let isLastPage = false;

$(document).ready(function() {
    const PERFORMANCE_ID = $('#chat-list-container').data('performance-id');

    if (!PERFORMANCE_ID) {
        console.error("performanceId가 설정되지 않았습니다.");
        return;
    }

    // 초기 로드
    loadChatList(0, PERFORMANCE_ID);

    // 검색 버튼/Enter 이벤트
    $('#searchBtn').click(() => resetAndSearch(PERFORMANCE_ID));
    $('#keyword').on('keypress', e => { if (e.key === 'Enter') resetAndSearch(PERFORMANCE_ID); });
    $('#excludeClosed').change(() => resetAndSearch(PERFORMANCE_ID));

    // 무한 스크롤
    $(window).scroll(() => {
        if ($(window).scrollTop() + $(window).height() > $(document).height() - 100) {
            if (!isLoading && !isLastPage) loadChatList(currentPage + 1, PERFORMANCE_ID);
        }
    });
});

// 검색 리셋 + 재조회
function resetAndSearch(performanceId) {
    $('#chat-list-container').empty();
    currentPage = 0;
    isLastPage = false;
    loadChatList(0, performanceId);
}

// AJAX 호출
function loadChatList(page, performanceId) {
    isLoading = true;

    $.ajax({
        url: `/api/performance/${performanceId}/list`, // Controller 매핑에 맞춤
        method: 'GET',
        data: {
            page: page,
            searchType: $('#searchType').val(),
            keyword: $('#keyword').val(),
            onlyOpen: $('#excludeClosed').is(':checked')
        },
        success: function(res) {
            const slice = res.data;

            if (!slice || !slice.content || slice.content.length === 0) {
                isLastPage = true;
                if (page === 0) $('#chat-list-container').html('<p class="text-center mt-5">검색 결과가 없습니다.</p>');
            } else {
                renderList(slice.content, performanceId);
                currentPage = page;
                isLastPage = slice.last;
            }
        },
        error: function(err) {
            console.error("데이터 로드 실패:", err);
            $('#chat-list-container').html('<p class="text-center mt-5 text-danger">채팅방을 불러오는 데 실패했습니다.</p>');
        },
        complete: function() { isLoading = false; }
    });
}

// 리스트 렌더링
function renderList(items, performanceId) {
    let html = '';
    items.forEach(chat => {
        const isClosed = chat.status === 'CLOSED';
        const statusClass = isClosed ? 'closed' : 'open';
        const statusText = isClosed ? '마감' : '모집중';

        html += `
        <div class="chat-item-card mb-2" onclick="location.href='/performance/${performanceId}/chat/${chat.id}'">
            <div class="chat-info">
                <div class="d-flex align-items-center mb-1">
                    <small class="performance-tag">${chat.performanceTitle}</small>
                    <span class="status-badge ${statusClass}">${statusText}</span>
                </div>
                <h5 class="chat-title">${chat.title}</h5>
                <div class="chat-meta">
                    <span class="divider">|</span>
                    <span class="date">${chat.updatedAt}</span>
                </div>
            </div>
            <div class="member-info">
                <div class="member-count">${chat.currentMember}/${chat.maxMember}</div>
            </div>
        </div>`;
    });

    $('#chat-list-container').append(html);
}
