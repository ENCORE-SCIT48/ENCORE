$(function () {
    const performanceId = $("#performanceId").val();
    const loginUserId = Number($("#loginUserId").val() || 0); // 내 리뷰 판별용

    let isWished = false;
    let isWatched = false;
    let isReported = false;

    let watchedChecked = false;

    let reviewLoaded = false;
    let reviewPage = 0;
    const reviewSize = 10;
    let reviewLast = true;

    let reviewPrefetched = false;
    let cachedReviewPage0 = null;

    function escapeHtml(str) {
        return String(str ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function bindTabs() {
        $(".tab-btn").on("click", function () {
            const tab = $(this).data("tab");

            $(".tab-btn").removeClass("active");
            $(this).addClass("active");

            $(".tab-content").removeClass("active");
            $(`#tab-${tab}`).addClass("active");

            if (tab === "review") {
                ensureWatchedStatus();
            }

            if (tab === "review" && !reviewLoaded) {
                if (cachedReviewPage0) {
                    renderReviews(cachedReviewPage0.content, true);

                    reviewLast = cachedReviewPage0.last;
                    reviewLoaded = true;

                    if (!reviewLast) $("#reviewMoreWrap").show();
                    else $("#reviewMoreWrap").hide();

                    if (!cachedReviewPage0.content || cachedReviewPage0.content.length === 0) {
                        $("#reviewEmpty").show();
                    }
                } else {
                    loadReviews(true);
                }
            }
        });
    }

    function statusToCategory(status) {
        const map = { MUSICAL: "뮤지컬", PLAY: "연극", BAND: "밴드 공연" };
        return map[status] || status || "-";
    }

    function setWishUI(on) {
        isWished = on;
        $("#wishIcon").attr("src", on ? "/image/wish_on.png" : "/image/wish_off.png");
        $("#wishBtn").attr("aria-pressed", String(on));
    }

    function toggleWriteReviewButton(on) {
        if (on) $("#writeReviewBtn").removeClass("d-none");
        else $("#writeReviewBtn").addClass("d-none");
    }

    function setWatchedUI(on) {
        isWatched = on;
        $("#watchedIcon").attr("src", on ? "/image/watched_on.png" : "/image/watched_off.png");
        $("#watchedBtn").attr("aria-pressed", String(on));
        toggleWriteReviewButton(on);
    }

    function setReportedUI(on) {
        isReported = on;
        $("#reportedIcon").attr("src", on ? "/image/reported_on.png" : "/image/reported_off.png");
        $("#reportedBtn").attr("aria-pressed", String(on));
    }

    function bindActionButtons() {
        $("#mapBtn").off("click").on("click", function () {
            alert("지도 기능은 추후 연결 예정");
        });

        $("#wishBtn").off("click").on("click", function () {
            setWishUI(!isWished);
        });

        $("#watchedBtn").off("click").on("click", function () {
            setWatchedUI(!isWatched);
            watchedChecked = true;
        });

        $("#reportedBtn").off("click").on("click", function () {
            setReportedUI(!isReported);
        });

        $("#writeReviewBtn").off("click").on("click", function () {
            alert("리뷰 작성 (준비중)");
        });
    }

    function renderDetail(d) {
        $("#categoryText").text(statusToCategory(d?.status ?? "-"));
        $("#perfTitle").text(escapeHtml(d?.title ?? "공연 제목"));

        const venueName = d?.venueName ?? "-";
        const address = d?.address ?? "-";
        $("#metaText").text(`${address} · ${venueName}`);

        $("#ratingText").text("-");
        $("#descText").text(escapeHtml(d?.description ?? "공연상세설명"));

        setWishUI(isWished);
        setWatchedUI(isWatched);
        setReportedUI(isReported);

        bindActionButtons();
        ensureWatchedStatus();
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

    function ensureWatchedStatus() {
        if (watchedChecked) return;

        $.ajax({
            url: `/api/performances/${performanceId}/watched`,
            method: "GET",
            dataType: "json",
            xhrFields: { withCredentials: true }
        })
            .done(function (res) {
                const watched = !!res?.data?.watched;
                setWatchedUI(watched);
                watchedChecked = true;
            })
            .fail(function () {
                setWatchedUI(false);
                watchedChecked = true;
            });
    }

    function buildReviewCard(r) {
        const nickname = escapeHtml(r?.nickname ?? "-");
        const rating = Number(r?.rating ?? 0);
        const content = escapeHtml(r?.content ?? "");
        const createdAt = escapeHtml(
            (r?.createdAt ?? "").replace("T", " ").substring(0, 16)
        );

        const reviewId = r?.reviewId;

        const reviewUserId = Number(r?.userId ?? 0);
        const isMine = (loginUserId > 0) && (reviewUserId === loginUserId);

        const stars = Array.from({ length: 5 }, (_, i) => (i < rating ? "★" : "☆")).join("");

        const actionHtml = isMine
            ? `
                <div class="review-actions">
                    <button type="button" class="review-action-btn js-review-edit" data-review-id="${reviewId}">
                        수정
                    </button>
                    <button type="button" class="review-action-btn danger js-review-delete" data-review-id="${reviewId}">
                        삭제
                    </button>
                </div>
              `
            : `
                <div class="review-actions">
                    <button type="button" class="review-action-btn js-review-report" data-review-id="${reviewId}">
                        신고
                    </button>
                </div>
              `;

        return `
            <div class="review-card">
                <div class="review-left">
                    <div class="review-topline">
                        <div class="review-stars">${stars}</div>
                        ${actionHtml}
                    </div>

                    <div class="review-meta">
                        <span class="review-nickname">${nickname}</span>
                        <span class="review-dot">·</span>
                        <span class="review-date">${createdAt}</span>
                    </div>

                    <div class="review-content">${content}</div>
                </div>

                <div class="review-right">
                    <div class="review-thumb"></div>
                </div>
            </div>
        `;
    }

    function calcAvgRating(items) {
        if (!items || items.length === 0) return null;
        const sum = items.reduce((acc, cur) => acc + Number(cur?.rating ?? 0), 0);
        return sum / items.length;
    }

    function setAvgRatingUI(avg) {
        if (avg == null) {
            $("#ratingText").text("-");
            return;
        }
        $("#ratingText").text(avg.toFixed(1));
    }

    function renderReviews(content, reset) {
        const $list = $("#reviewList");

        if (reset) {
            $list.empty();
            $("#reviewEmpty").hide();
        }

        if (!content || content.length === 0) {
            if (reset) $("#reviewEmpty").show();
            return;
        }

        $list.append(content.map(buildReviewCard).join(""));
    }

    function loadReviews(reset) {
        if (reset) {
            reviewPage = 0;
            reviewLast = true;
            reviewLoaded = false;
            $("#reviewMoreWrap").hide();
            $("#reviewEmpty").hide();
            $("#reviewList").empty();
        }

        $.ajax({
            url: `/api/performances/${performanceId}/reviews`,
            method: "GET",
            dataType: "json",
            data: { page: reviewPage, size: reviewSize },
            xhrFields: { withCredentials: true }
        })
            .done(function (res) {
                const pageData = res?.data;
                if (!pageData) {
                    console.error("[reviews] data 없음", res);
                    $("#reviewEmpty").show().text("응답 형식 오류(data 없음)");
                    return;
                }

                const content = pageData?.content ?? [];
                const last = !!pageData?.last;

                setAvgRatingUI(calcAvgRating(content));
                renderReviews(content, reset);

                reviewLast = last;
                reviewLoaded = true;

                if (!reviewLast) $("#reviewMoreWrap").show();
                else $("#reviewMoreWrap").hide();
            })
            .fail(function (xhr) {
                console.error("[reviews] 조회 실패", xhr);
                $("#reviewEmpty").show().text("후기 조회 실패");
            });
    }

    function preloadReviewRating() {
        if (reviewPrefetched) return;

        $.ajax({
            url: `/api/performances/${performanceId}/reviews`,
            method: "GET",
            dataType: "json",
            data: { page: 0, size: reviewSize },
            xhrFields: { withCredentials: true }
        })
            .done(function (res) {
                const pageData = res?.data;
                if (!pageData) return;

                const content = pageData?.content ?? [];
                const last = !!pageData?.last;

                setAvgRatingUI(calcAvgRating(content));
                cachedReviewPage0 = { content, last };

                reviewPrefetched = true;
            })
            .fail(function () {
                setAvgRatingUI(null);
            });
    }

    $("#reviewMoreBtn").off("click").on("click", function () {
        if (reviewLast) return;
        reviewPage += 1;
        loadReviews(false);
    });

    $(document).on("click", ".js-review-report, .js-review-edit, .js-review-delete", function (e) {
        e.preventDefault();
        e.stopPropagation();
        // 기능은 아직 연결하지 않음
    });

    bindTabs();
    loadDetail();
    preloadReviewRating();
});
