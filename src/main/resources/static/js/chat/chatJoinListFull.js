let page = 0;
const size = 20;
let loading = false;
let hasNext = true;

function loadChats(reset = false) {
    if (loading || (!reset && !hasNext)) return;

    loading = true;

    if (reset) {
        page = 0;
        hasNext = true;
        $("#joined-chat-container").empty();
    }

    $.ajax({
        url: "/api/chat/join/full",
        method: "GET",
        data: {
            page: page,
            size: size,
            keyword: $("#keyword").val().trim() || "",
            searchType: $("#searchType").val()
        },
        success: function(response) {
            const slice = response.data;
            if (!slice) return;

            const data = slice.content || [];
            hasNext = slice.hasNext;

            if (data.length === 0 && reset) {
                $("#joined-chat-container").append('<div class="no-result">검색 결과가 없습니다.</div>');
            }

            data.forEach(chat => {
                const date = dayjs(chat.updatedAt).format("YY.MM.DD HH:mm");
                 const isClosed = chat.status === 'CLOSED';
                                                    const statusClass = isClosed ? 'closed' : 'open';
                                                    const statusText = isClosed ? '마감' : '모집중';


                $("#joined-chat-container").append(`
                    <div class="chat-item" onclick="location.href='/performance/${chat.performanceId}/chat/${chat.id}'">
                        <span class="status-badge ${statusClass}">${statusText}</span>
                        <small class="performance-tag">${chat.performanceTitle || '공연 정보 없음'}</small>
                        <div class="chat-title">${chat.title}</div>
                        <div class="chat-info">
                        <span>${date}</span>
                            <span><i class="fa-solid fa-users"></i> ${chat.currentMember}/${chat.maxMember}명</span>

                        </div>
                    </div>
                `);
            });

            page++;
        },
        error: function(xhr) {
            console.error("채팅방 조회 실패", xhr);
        },
        complete: function() {
            loading = false;
        }
    });
}

$(document).ready(function () {
    loadChats(true);
    $("#searchBtn").off("click").on("click", function (e) {
        e.preventDefault();
        loadChats(true);
    });
    $("#keyword").on("keydown", function (e) {
        if (e.keyCode === 13) {
            e.preventDefault();
            loadChats(true);
        }
    });
    $(window).on("scroll", function () {
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 100) {
            loadChats();
        }
    });
});
