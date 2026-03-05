$(function () {
    const state = {
        page: 0,
        size: 9,
        keyword: "",
        category: "",
        filter: "",     // BOOKMARK / VIEWED
        loading: false,
        last: false
    };

    function escapeHtml(str) {
        return String(str ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function renderCards(items, append) {
        if (!append) $("#performanceGrid").empty();

        items.forEach(function (p) {
            const id = p.performanceId;
            if (!id) return;

            const title = p.title ?? "공연";

            const imageUrl = escapeHtml(p.performanceImageUrl || "");

            const reviewButtons = (state.filter === "VIEWED")
                ? `
                    <div class="review-actions">
                        <button
                            type="button"
                            class="review-btn review-btn--perf js-perf-review-btn"
                            data-performance-id="${id}"
                        >
                            공연리뷰
                        </button>
                        <button
                            type="button"
                            class="review-btn review-btn--seat js-seat-review-btn"
                            data-performance-id="${id}"
                        >
                            좌석리뷰
                        </button>
                    </div>
                  `
                : "";

            const card = `
                <div class="col-4">
                    <a class="d-block text-decoration-none" href="/performances/${id}">
                        <div class="perf-card">
                        <img src="${imageUrl}" alt="${escapeHtml(title)}" class="perf-card__img"></div>
                        <div class="mt-2 small text-dark text-truncate">${escapeHtml(title)}</div>
                    </a>
                    ${reviewButtons}
                </div>
            `;

            $("#performanceGrid").append(card);
        });
    }

    function resolveApiUrl() {
        if (state.filter === "VIEWED") return "/api/performances/watched";
        if (state.filter === "BOOKMARK") return "/api/performances/wished";
        return "/api/performances";
    }

    // 어떤 탭이든 keyword는 보낼 수 있게 통일
    function buildRequestData() {
        const base = {
            page: state.page,
            size: state.size,
            keyword: state.keyword || null
        };

        // 전체/카테고리 탭일 때만 category를 추가로 보냄
        if (!state.filter) {
            base.category = state.category || null;
        }

        return base;
    }

    function normalizeItemsFromResponse(res) {
        const data = res?.data;

        if (!data) return { items: [], last: true };

        if (Array.isArray(data.content)) {
            return { items: data.content, last: !!data.last };
        }

        if (Array.isArray(data)) {
            return { items: data, last: true };
        }

        return { items: [], last: true };
    }

    function fetchList(append) {
        if (state.loading || state.last) return;

        state.loading = true;

        $.ajax({
            url: resolveApiUrl(),
            method: "GET",
            dataType: "json",
            data: buildRequestData(),
            xhrFields: { withCredentials: true }
        })
            .done(function (res) {
                const normalized = normalizeItemsFromResponse(res);
                const items = normalized.items || [];

                state.last = !!normalized.last;

                renderCards(items, append);

                if (!state.last) state.page += 1;

                // 더보기 버튼: 마지막 페이지면 숨김, 아니면 표시
                var $loadMore = $("#loadMoreBtn");
                if ($loadMore.length) $loadMore.toggle(!state.last);
            })
            .fail(function (xhr) {
                const status = xhr.status;
                const text = xhr.responseText || "";
                console.error("[list] 목록 조회 실패", "status=" + status, text);
                if (status === 500) {
                    alert("목록 조회 실패 (서버 오류). 터미널/서버 로그를 확인하고, data-test-insert.sql 실행 여부와 DB 스키마를 확인하세요.");
                } else {
                    alert("목록 조회 실패 (HTTP " + status + ")");
                }
            })
            .always(function () {
                state.loading = false;
            });
    }

    function resetAndLoad() {
        state.page = 0;
        state.last = false;
        $("#loadMoreBtn").show(); // 탭/검색 바꿀 때 다시 보이게
        fetchList(false);
    }

    // 북마크/본공연 탭에서도 검색 가능하게: 강제 초기화 삭제
    $("#searchBtn").on("click", function () {
        state.keyword = $("#keyword").val().trim();
        resetAndLoad();
    });

    $("#keyword").on("keydown", function (e) {
        if (e.key === "Enter") $("#searchBtn").click();
    });

    $(document).on("click", ".tab-btn", function () {
        $(".tab-btn").removeClass("active");
        $(this).addClass("active");

        const filter = $(this).data("filter");
        const category = $(this).data("category");

        // filter 탭(북마크/본공연)
        if (typeof filter !== "undefined") {
            state.filter = filter || "";
            state.category = "";
            resetAndLoad();
            return;
        }

        // 카테고리 탭
        state.filter = "";
        state.category = category || "";
        resetAndLoad();
    });

    $(document).on("click", "#loadMoreBtn", function () {
        fetchList(true);
    });

    // 공연리뷰 -> 공연 리뷰 작성 페이지로 이동
    $(document).on("click", ".js-perf-review-btn", function (e) {
        e.preventDefault();
        e.stopPropagation();

        const id = $(this).data("performance-id");
        if (!id) return;

        window.location.href = `/performances/${id}/reviews/new`;
    });

    // 좌석리뷰 -> 좌석 리뷰 작성 페이지로 이동
    $(document).on("click", ".js-seat-review-btn", function (e) {
        e.preventDefault();
        e.stopPropagation();

        const id = $(this).data("performance-id");
        if (!id) return;

        window.location.href = `/performances/${id}/reviews/seats/new`;
    });

    resetAndLoad();
});
