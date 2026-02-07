let currentPage = 0;
let isLoading = false;
let isLastPage = false;

$(document).ready(function() {
    const PERFORMANCE_ID = $('#chat-list-container').data('performance-id');

    if (!PERFORMANCE_ID) {
        console.error("performanceId가 설정되지 않았습니다.");
        return;
    }

    loadChatList(0, PERFORMANCE_ID);

    $('#searchBtn').click(() => resetAndSearch(PERFORMANCE_ID));
    $('#keyword').on('keypress', e => { if (e.key === 'Enter') resetAndSearch(PERFORMANCE_ID); });
    $('#excludeClosed').change(() => resetAndSearch(PERFORMANCE_ID));

    $(window).scroll(() => {
        if ($(window).scrollTop() + $(window).height() > $(document).height() - 100) {
            if (!isLoading && !isLastPage) loadChatList(currentPage + 1, PERFORMANCE_ID);
        }
    });
});

function resetAndSearch(performanceId) {
    $('#chat-list-container').empty();
    currentPage = 0;
    isLastPage = false;
    loadChatList(0, performanceId);
}

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

function renderList(items, performanceId) {
    let html = '';
    items.forEach(chat => {
        const isClosed = chat.status === 'CLOSED';
        const statusClass = isClosed ? 'closed' : 'open';
        const statusText = isClosed ? '마감' : '모집중';

        $('#chat-list-container').append(`
        <div class="chat-item"
             onclick="location.href='/performance/${performanceId}/chat/${chat.id}'">
          <span class="status-badge ${statusClass}">
            ${statusText}
          </span>
          <div class="chat-title">
            ${chat.title}
          </div>
          <div class="chat-bottom">
            <span class="date">${chat.updatedAt}</span>
            <span><i class="fa-solid fa-users"></i>
              ${chat.currentMember}/${chat.maxMember}명
            </span>
          </div>
        </div>

        `);
    });


}
