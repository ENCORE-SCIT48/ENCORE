$(function () {
    const state = {
        page: 0,
        size: 9,
        keyword: "",
        category: "",
        filter: "",
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

            const card = `
                <div class="col-4">
                    <a class="d-block text-decoration-none" href="/performances/${id}">
                        <div class="perf-card"></div>
                        <div class="mt-2 small text-dark text-truncate">${escapeHtml(title)}</div>
                    </a>
                </div>
            `;

            $("#performanceGrid").append(card);
        });
    }

    function fetchList(append) {
        if (state.loading || state.last) {
            return;
        }

        state.loading = true;

        $.ajax({
            url: "/api/performances",
            method: "GET",
            dataType: "json",
            data: {
                keyword: state.keyword || null,
                category: state.category || null,
                page: state.page,
                size: state.size
            }
        })
            .done(function (res) {
                const pageObj = res?.data;

                if (!pageObj) {
                    console.error("[performances] data 없음", res);
                    alert("응답 형식 오류");
                    return;
                }

                const items = pageObj.content || [];
                state.last = !!pageObj.last;

                renderCards(items, append);

                if (!state.last) {
                    state.page += 1;
                }
            })
            .fail(function (xhr) {
                console.error("[performances] 목록 조회 실패", xhr);
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

        const category = $(this).data("category");
        if (typeof category !== "undefined") {
            state.category = category || "";
        }

        resetAndLoad();
    });

    $(document).on("click", "#loadMoreBtn", function () {
        fetchList(true);
    });

    resetAndLoad();
});
