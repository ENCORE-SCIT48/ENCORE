let page = 0;
const size = 20;
let loading = false;
let hasNext = true;

function loadChats(reset = false) {
    // [중요] reset이 true일 때는 hasNext가 false여도 무시하고 실행해야 합니다.
    if (loading || (!reset && !hasNext)) return;

    loading = true;

    if (reset) {
        page = 0;
        hasNext = true; // [중요] 다시 검색할 때는 상태를 초기화
        $("#joined-chat-container").empty();
    }

    $.ajax({
        url: "/api/chat/join/full",
        method: "GET",
        data: {
            page: page,
            size: size,
            // 빈 문자열 전달 시 서버에서 trim 처리되도록
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
                $("#joined-chat-container").append(`
                    <div class="chat-item" onclick="location.href='/performance/${chat.performanceId}/chat/${chat.id}'">
                        <div class="performance-title">${chat.performanceTitle || '공연 정보 없음'}</div>
                        <div class="chat-title">${chat.title}</div>
                        <div class="chat-info">
                            <span><i class="fa-solid fa-users"></i> ${chat.currentMember}/${chat.maxMember}명</span>
                            <span>${date}</span>
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
            loading = false; // 성공하든 실패하든 로딩 상태는 해제해야 다음 작업이 가능함
        }
    });
}

$(document).ready(function () {
    // 1. 초기 실행
    loadChats(true);

    // 2. 검색 버튼 클릭 (반응 없을 때 대비해서 확실히 연결)
    $("#searchBtn").off("click").on("click", function (e) {
        e.preventDefault();
        loadChats(true);
    });

    // 3. 엔터키 입력 시 검색 실행
    $("#keyword").on("keydown", function (e) {
        if (e.keyCode === 13) {
            e.preventDefault();
            loadChats(true);
        }
    });

    // 4. 스크롤 이벤트 (윈도우 기준이 더 정확할 수 있습니다)
    $(window).on("scroll", function () {
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 100) {
            loadChats();
        }
    });
});
