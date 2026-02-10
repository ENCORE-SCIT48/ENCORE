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
        return String(str)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function renderCards(items, append) {
        if (!append) {
            $("#performanceGrid").empty();
        }

        items.forEach(function (p) {
            const id = p.performanceId;
            if (!id) return;

            const title = p.title ?? "공연";

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
                        <div class="perf-card"></div>
                        <div class="mt-2 small text-dark text-truncate">${escapeHtml(title)}</div>
                    </a>
                    ${reviewButtons}
                </div>
            `;

            $("#performanceGrid").append(card);
        });
    }

    function resolveApiUrl() {
        if (state.filter === "VIEWED") {
            return "/api/performances/watched";
        }
        return "/api/performances";
    }

    function buildRequestData() {
        if (state.filter === "VIEWED") {
            return { page: state.page, size: state.size };
        }

        return {
            keyword: state.keyword || null,
            category: state.category || null,
            page: state.page,
            size: state.size
        };
    }

    function normalizeItemsFromResponse(res) {
        const data = res?.data;

        if (!data) {
            return { items: [], last: true };
        }

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

                if (!state.last) {
                    state.page += 1;
                }
            })
            .fail(function (xhr) {
                console.error("[list] 목록 조회 실패", xhr);
                alert("목록 조회 실패");
            })
            .always(function () {
                state.loading = false;
            });
    }

    function resetAndLoad() {
        state.page = 0;
        state.last = false;
        fetchList(false);
    }

    $("#searchBtn").on("click", function () {
        state.keyword = $("#keyword").val().trim();
        resetAndLoad();
    });

    $("#keyword").on("keydown", function (e) {
        if (e.key === "Enter") {
            $("#searchBtn").click();
        }
    });

    $(document).on("click", ".tab-btn", function () {
        $(".tab-btn").removeClass("active");
        $(this).addClass("active");

        const filter = $(this).data("filter");
        const category = $(this).data("category");

        if (typeof filter !== "undefined") {
            state.filter = filter || "";
            state.category = "";
            state.keyword = "";
            $("#keyword").val("");
            resetAndLoad();
            return;
        }

        state.filter = "";
        state.category = category || "";
        resetAndLoad();
    });

    $(document).on("click", "#loadMoreBtn", function () {
        fetchList(true);
    });

    // 버튼 기능은 아직 없음
    $(document).on("click", ".js-perf-review-btn, .js-seat-review-btn", function (e) {
        e.preventDefault();
        e.stopPropagation();
    });

    resetAndLoad();
});
