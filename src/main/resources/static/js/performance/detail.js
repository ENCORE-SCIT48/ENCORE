$(function () {
    const performanceId = $("#performanceId").val();

    // 상태값: renderDetail 밖에서 관리 (재렌더/재호출 시 초기화 방지)
    let isWished = false;
    let isWatched = false;
    let isReported = false;

    // 리뷰 조회 상태(중복 호출 방지 + 더보기)
    let reviewLoaded = false;
    let reviewPage = 0;
    const reviewSize = 10;
    let reviewLast = true;

    // 리뷰 프리로드(평점 미리 표시용)
    let reviewPrefetched = false;
    let cachedReviewPage0 = null; // { content: [], last: true }

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

            // 공연후기 탭 진입 시 리뷰 조회
            if (tab === "review" && !reviewLoaded) {
                // 캐시가 있으면 API 재호출 없이 바로 렌더
                if (cachedReviewPage0) {
                    renderReviews(cachedReviewPage0.content, true);

                    // 더보기 버튼 처리
                    reviewLast = cachedReviewPage0.last;
                    reviewLoaded = true;

                    if (!reviewLast) {
                        $("#reviewMoreWrap").show();
                    } else {
                        $("#reviewMoreWrap").hide();
                    }

                    // 빈 상태 처리
                    if (!cachedReviewPage0.content || cachedReviewPage0.content.length === 0) {
                        $("#reviewEmpty").show();
                    }
                } else {
                    // 캐시 없으면 기존대로 호출
                    loadReviews(true);
                }
            }
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

        // 상단 카테고리 텍스트
        $("#categoryText").text(statusToCategory(status));

        // 제목
        $("#perfTitle").text(escapeHtml(title));

        // 메타: 주소 · 공연장명 (DTO에 추가됨)
        const venueName = d?.venueName ?? "-";
        const address = d?.address ?? "-";
        $("#metaText").text(`${address} · ${venueName}`);

        // 평점(초기)
        $("#ratingText").text("-");

        // 상세설명
        $("#descText").text(escapeHtml(description));

        // 초기 UI 상태(현재는 기본 off)
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

    // 리뷰 카드 HTML 생성
    function buildReviewCard(r) {
        const nickname = escapeHtml(r?.nickname ?? "-");
        const rating = Number(r?.rating ?? 0);
        const content = escapeHtml(r?.content ?? "");
        const createdAt = escapeHtml(
            (r?.createdAt ?? "")
                .replace("T", " ")
                .substring(0, 16) // YYYY-MM-DD HH:mm
        );

        // 별점(5개)
        const stars = Array.from({ length: 5 }, (_, i) => (i < rating ? "★" : "☆")).join("");

        return `
            <div class="review-card">
                <div class="review-left">
                    <div class="review-stars">${stars}</div>
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

    // 평균 평점 계산(현재 페이지 기준)
    function calcAvgRating(items) {
        if (!items || items.length === 0) return null;

        const sum = items.reduce((acc, cur) => acc + Number(cur?.rating ?? 0), 0);
        return sum / items.length;
    }

    // 탭 버튼 옆 평점 업데이트
    function setAvgRatingUI(avg) {
        if (avg == null) {
            $("#ratingText").text("-");
            return;
        }

        $("#ratingText").text(avg.toFixed(1));
    }

    // 리뷰 목록 렌더
    function renderReviews(content, reset) {
        const $list = $("#reviewList");

        if (reset) {
            $list.empty();
            $("#reviewEmpty").hide();
        }

        if (!content || content.length === 0) {
            if (reset) {
                $("#reviewEmpty").show();
            }
            return;
        }

        const html = content.map(buildReviewCard).join("");
        $list.append(html);
    }

    // 리뷰 조회
    function loadReviews(reset) {
        // reset=true면 첫 페이지부터 다시
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
            data: {
                page: reviewPage,
                size: reviewSize,
            },
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

                // 평균 평점(현재는 1페이지 기준)
                const avg = calcAvgRating(content);
                setAvgRatingUI(avg);

                // 리스트 렌더
                renderReviews(content, reset);

                // 더보기 상태
                reviewLast = last;
                reviewLoaded = true;

                if (!reviewLast) {
                    $("#reviewMoreWrap").show();
                } else {
                    $("#reviewMoreWrap").hide();
                }
            })
            .fail(function (xhr) {
                console.error("[reviews] 조회 실패", xhr);
                $("#reviewEmpty").show().text("후기 조회 실패");
            });
    }

    // 페이지 진입 시 - 공연후기 탭 버튼 옆 평점만 미리 세팅
    function preloadReviewRating() {
        if (reviewPrefetched) return;

        $.ajax({
            url: `/api/performances/${performanceId}/reviews`,
            method: "GET",
            dataType: "json",
            data: { page: 0, size: reviewSize },
        })
            .done(function (res) {
                const pageData = res?.data;
                if (!pageData) return;

                const content = pageData?.content ?? [];
                const last = !!pageData?.last;

                // 평균 평점 (현재는 1페이지 기준)
                const avg = calcAvgRating(content);
                setAvgRatingUI(avg);

                // 0페이지 캐싱(탭 들어갔을 때 바로 리스트 뿌리기)
                cachedReviewPage0 = { content, last };

                reviewPrefetched = true;
            })
            .fail(function () {
                setAvgRatingUI(null);
            });
    }

    // 더보기 버튼
    $("#reviewMoreBtn").off("click").on("click", function () {
        if (reviewLast) return;

        reviewPage += 1;
        loadReviews(false);
    });

    bindTabs();
    loadDetail();
    preloadReviewRating();
});
