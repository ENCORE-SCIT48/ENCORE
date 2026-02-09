$(function () {
    const performanceId = $("#performanceId").val();

    // 상태값: renderDetail 밖에서 관리 (재렌더/재호출 시 초기화 방지)
    let isWished = false;
    let isWatched = false;
    let isReported = false;

    function escapeHtml(str) {
        return String(str ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    // 탭 UI
    function bindTabs() {
        $(".tab-btn").on("click", function () {
            const tab = $(this).data("tab");

            $(".tab-btn").removeClass("active");
            $(this).addClass("active");

            $(".tab-content").removeClass("active");
            $(`#tab-${tab}`).addClass("active");
        });
    }

    // status enum -> 화면 표시용(임시)
    function statusToCategory(status) {
        const map = {
            MUSICAL: "뮤지컬",
            PLAY: "연극",
            BAND: "밴드 공연",
        };
        return map[status] || status || "-";
    }

    // wish UI 세팅
    function setWishUI(on) {
        isWished = on;

        const imgPath = on ? "/image/wish_on.png" : "/image/wish_off.png";
        $("#wishIcon").attr("src", imgPath);

        $("#wishBtn").attr("aria-pressed", String(on));
    }

    // watched UI 세팅
    function setWatchedUI(on) {
        isWatched = on;

        const imgPath = on ? "/image/watched_on.png" : "/image/watched_off.png";
        $("#watchedIcon").attr("src", imgPath);

        $("#watchedBtn").attr("aria-pressed", String(on));
    }

    // reported UI 세팅
    function setReportedUI(on) {
        isReported = on;

        const imgPath = on ? "/image/reported_on.png" : "/image/reported_off.png";
        $("#reportedIcon").attr("src", imgPath);

        $("#reportedBtn").attr("aria-pressed", String(on));
    }

    function bindActionButtons() {
        // 지도 버튼
        $("#mapBtn").off("click").on("click", function () {
            alert("지도 기능은 추후 연결 예정");
        });

        // 찜 버튼 토글
        $("#wishBtn").off("click").on("click", function () {
            setWishUI(!isWished);

            // API 연동 시 여기에서 처리
            // POST/DELETE /api/performances/{id}/wish
        });

        // 본공연 버튼 토글
        $("#watchedBtn").off("click").on("click", function () {
            setWatchedUI(!isWatched);

            // API 연동 시 여기에서 처리
            // POST/DELETE /api/performances/{id}/watched
        });

        // 신고 버튼 토글
        $("#reportedBtn").off("click").on("click", function () {
            setReportedUI(!isReported);

            // API 연동 시 여기에서 처리
            // 보통 신고는 1회성이라 ON 되면 다시 OFF 못하게 막는 경우가 많음
            // POST /api/performances/{id}/report
        });
    }

    function renderDetail(d) {
        const title = d?.title ?? "공연 제목";
        const description = d?.description ?? "공연상세설명";
        const status = d?.status ?? "-";
        const capacity = d?.capacity ?? null;

        // 상단 카테고리 텍스트
        $("#categoryText").text(statusToCategory(status));

        // 제목
        $("#perfTitle").text(escapeHtml(title));

        // 메타: 주소 · 공연장명 (DTO에 추가됨)
        const venueName = d?.venueName ?? "-";
        const address = d?.address ?? "-";
        $("#metaText").text(`${address} · ${venueName}`);

        // 평점(현재 DTO에 없음)
        $("#ratingText").text("-");

        // 상세설명
        $("#descText").text(escapeHtml(description));

        // 초기 UI 상태(현재는 기본 off)
        // 나중에 서버에서 wish/watched/reported 여부 내려주면 여기 변경 예정
        setWishUI(isWished);
        setWatchedUI(isWatched);
        setReportedUI(isReported);

        // 버튼 바인딩
        bindActionButtons();
    }

    function loadDetail() {
        $.ajax({
            url: `/api/performances/${performanceId}`,
            method: "GET",
            dataType: "json",
        })
            .done(function (res) {
                const data = res?.data;

                if (!data) {
                    console.error("[performance detail] data 없음", res);
                    $("#descText").text("응답 형식 오류(data 없음)");
                    return;
                }

                renderDetail(data);
            })
            .fail(function (xhr) {
                console.error("[performance detail] 상세 조회 실패", xhr);
                $("#descText").text("상세 조회 실패");
            });
    }

    bindTabs();
    loadDetail();
});
