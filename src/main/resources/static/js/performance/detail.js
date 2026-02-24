$(function () {
    const performanceId = $("#performanceId").val();
    const loginUserId = Number($("#loginUserId").val() || 0); // 내 리뷰 판별용

    let isWished = false;
    let isWatched = false;
    let isReported = false;
    let watchedChecked = false;
    let wishedChecked = false;

    let reviewLoaded = false;
    let reviewPage = 0;
    const reviewSize = 10;
    let reviewLast = true;

    let reviewPrefetched = false;
    let cachedReviewPage0 = null;

    let wishRequesting = false; // 중복 클릭 방지
    let watchedRequesting = false; // 중복 클릭 방지

    // 리뷰 정렬: latest(기본) / rating
    let reviewSort = "latest";

    // 리뷰 상세 모달/캐시
    const reviewCache = new Map(); // reviewId -> review 객체
    let reviewModal = null;        // bootstrap modal instance

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

            // 리뷰 탭 최초 진입 시: 캐시 우선 렌더 -> 없으면 API 호출
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
            if (wishRequesting) return;
            wishRequesting = true;

            $.ajax({
                url: `/api/performances/${performanceId}/wish`,
                method: "POST",
                dataType: "json",
                xhrFields: { withCredentials: true }
            })
                .done(function (res) {
                    const wished = !!res?.data?.wished;
                    setWishUI(wished);
                    wishedChecked = true;
                })
                .fail(function (xhr) {
                    alert("찜 처리 실패");
                })
                .always(function () {
                    wishRequesting = false;
                });
        });

        $("#watchedBtn").off("click").on("click", function () {
            if (watchedRequesting) return;
            watchedRequesting = true;

            $.ajax({
                url: `/api/performances/${performanceId}/watched`,
                method: "POST",
                dataType: "json",
                xhrFields: { withCredentials: true }
            })
                .done(function (res) {
                    const watched = !!res?.data?.watched;
                    setWatchedUI(watched);
                    watchedChecked = true;
                })
                .fail(function (xhr) {
                    alert("본 공연 처리 실패");
                })
                .always(function () {
                    watchedRequesting = false;
                });
        });

        $("#reportedBtn").off("click").on("click", function () {
            setReportedUI(!isReported);
        });

        $("#writeReviewBtn").off("click").on("click", function () {
            // 리뷰 작성 페이지로 이동
            window.location.href = `/performances/${performanceId}/reviews/new`;
        });
    }

    function renderDetail(d) {
        $("#categoryText").text(statusToCategory(d?.status ?? "-"));
        $("#perfTitle").text(escapeHtml(d?.title ?? "공연 제목"));

        const venueName = d?.venueName ?? "-";
        const address = d?.address ?? "-";
        $("#metaText").text(`${address} · ${venueName}`);

        // 평점은 "summary API"로만 세팅(정렬/페이지에 따라 흔들리지 않게)
        $("#descText").text(escapeHtml(d?.description ?? "공연상세설명"));

        setWishUI(isWished);
        setWatchedUI(isWatched);
        setReportedUI(isReported);

        bindActionButtons();

        // 초기 상태 동기화
        ensureWishStatus();
        ensureWatchedStatus();
        loadReviewSummary();
    }

    function loadDetail() {
        $.ajax({
            url: `/api/performances/${performanceId}`,
            method: "GET",
            dataType: "json"
        })
            .done(function (res) {
                const data = res?.data;
                if (!data) {
                    $("#descText").text("응답 형식 오류(data 없음)");
                    return;
                }
                renderDetail(data);
            })
            .fail(function (xhr) {
                $("#descText").text("상세 조회 실패");
            });
    }

    // 공연 전체 평균/개수 요약 조회 (평점 표시용)
    function loadReviewSummary() {
        $.ajax({
            url: `/api/performances/${performanceId}/reviews/summary`,
            method: "GET",
            dataType: "json"
        })
            .done(function (res) {
                const count = Number(res?.data?.reviewCount ?? 0);

                // 리뷰가 0개면 "-" 표시
                if (count === 0) {
                    $("#ratingText").text("-");
                    return;
                }

                const avgRaw = res?.data?.avgRating;

                if (avgRaw == null) {
                    $("#ratingText").text("-");
                    return;
                }

                const avg = Number(avgRaw);

                if (Number.isNaN(avg)) {
                    $("#ratingText").text("-");
                    return;
                }

                $("#ratingText").text(avg.toFixed(1));
            })
            .fail(function (xhr) {
                $("#ratingText").text("-");
            });
    }

    // 찜 여부 초기 조회
    function ensureWishStatus() {
        if (wishedChecked) return;

        $.ajax({
            url: `/api/performances/${performanceId}/wish`,
            method: "GET",
            dataType: "json",
            xhrFields: { withCredentials: true }
        })
            .done(function (res) {
                const wished = !!res?.data?.wished;
                setWishUI(wished);
                wishedChecked = true;
            })
            .fail(function () {
                setWishUI(false);
                wishedChecked = true;
            });
    }

    // 본 공연 여부 초기 조회
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

    // 리뷰 상세 모달 오픈
    function openReviewModal(reviewId) {
        const r = reviewCache.get(String(reviewId));
        if (!r) return;

        const rating = Number(r?.rating ?? 0);
        const stars = Array.from({ length: 5 }, (_, i) => (i < rating ? "★" : "☆")).join("");

        const nickname = r?.nickname ?? "-";
        const createdAt = (r?.createdAt ?? "").replace("T", " ").substring(0, 16);
        const content = r?.content ?? "";

        $("#modalStars").text(stars);
        $("#modalMeta").text(`${nickname} · ${createdAt}`);
        $("#modalContent").text(content);

        if (!reviewModal) {
            reviewModal = new bootstrap.Modal(document.getElementById("reviewDetailModal"));
        }
        reviewModal.show();
    }

    function buildReviewCard(r) {
        const nickname = escapeHtml(r?.nickname ?? "-");
        const rating = Number(r?.rating ?? 0);
        const content = escapeHtml(r?.content ?? "");
        const createdAt = escapeHtml((r?.createdAt ?? "").replace("T", " ").substring(0, 16));

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
            <div class="review-card" data-review-id="${reviewId}">
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

        // 모달용 캐시 저장
        content.forEach(function (r) {
            if (r?.reviewId != null) reviewCache.set(String(r.reviewId), r);
        });

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
            data: { page: reviewPage, size: reviewSize, sort: reviewSort },
            xhrFields: { withCredentials: true }
        })
            .done(function (res) {
                const pageData = res?.data;
                if (!pageData) {
                    $("#reviewEmpty").show().text("응답 형식 오류(data 없음)");
                    return;
                }

                const content = pageData?.content ?? [];
                const last = !!pageData?.last;

                renderReviews(content, reset);

                reviewLast = last;
                reviewLoaded = true;

                if (!reviewLast) $("#reviewMoreWrap").show();
                else $("#reviewMoreWrap").hide();
            })
            .fail(function (xhr) {
                $("#reviewEmpty").show().text("후기 조회 실패");
            });
    }

    // 리뷰 탭 첫 진입 UX 위해 0페이지 미리 받아두기(리스트만 캐시)
    function preloadReviewPage0() {
        if (reviewPrefetched) return;

        $.ajax({
            url: `/api/performances/${performanceId}/reviews`,
            method: "GET",
            dataType: "json",
            data: { page: 0, size: reviewSize, sort: reviewSort },
            xhrFields: { withCredentials: true }
        })
            .done(function (res) {
                const pageData = res?.data;
                if (!pageData) return;

                const content = pageData?.content ?? [];
                const last = !!pageData?.last;

                cachedReviewPage0 = { content, last };
                reviewPrefetched = true;

                // 프리패치 데이터도 캐시에 저장(탭 첫 진입 모달 대비)
                content.forEach(function (r) {
                    if (r?.reviewId != null) reviewCache.set(String(r.reviewId), r);
                });
            })
            .fail(function () {
                cachedReviewPage0 = null;
                reviewPrefetched = true;
            });
    }

    $("#reviewSort").off("change").on("change", function () {
        reviewSort = $(this).val() || "latest";

        // 정렬 변경 시: 캐시/상태 리셋
        reviewPage = 0;
        reviewLast = true;
        reviewLoaded = false;

        cachedReviewPage0 = null;
        reviewPrefetched = false;

        $("#reviewMoreWrap").hide();
        $("#reviewEmpty").hide();
        $("#reviewList").empty();

        // 정렬 바뀌면 캐시도 초기화(리스트 내용 달라짐)
        reviewCache.clear();

        // 리뷰 리스트만 다시 로드 (평균은 summary로 고정이라 다시 계산/갱신 불필요)
        loadReviews(true);
        preloadReviewPage0();
    });

    $("#reviewMoreBtn").off("click").on("click", function () {
        if (reviewLast) return;
        reviewPage += 1;
        loadReviews(false);
    });

    // 리뷰 카드 클릭 -> 상세 모달 오픈
    $(document).on("click", ".review-card", function (e) {
        // 액션 버튼(수정/삭제/신고) 눌렀으면 모달 열지 않음
        if ($(e.target).closest(".review-action-btn").length) return;

        const reviewId = $(this).data("review-id");
        if (!reviewId) return;

        openReviewModal(reviewId);
    });

    // 수정 버튼 클릭 -> 수정 화면으로 이동
    $(document).on("click", ".js-review-edit", function (e) {
        e.stopPropagation();

        const reviewId = $(this).data("review-id");
        if (!reviewId) return;

       window.location.href = `/performances/${performanceId}/reviews/${reviewId}/edit`;
    });

    $(document).on("click", ".js-review-delete", function (e) {
        e.stopPropagation();

        const reviewId = $(this).data("review-id");
        if (!reviewId) return;

        if (!confirm("리뷰를 삭제할까요?")) return;

        $.ajax({
            url: `/api/performances/${performanceId}/reviews/${reviewId}`,
            method: "DELETE",
            dataType: "json",
            xhrFields: { withCredentials: true }
        }).done(function () {
            // 1) 캐시에서도 제거
            reviewCache.delete(String(reviewId));

            // 2) 리스트/요약 다시 로드 (가장 안전)
            loadReviews(true);
            loadReviewSummary();
        }).fail(function (xhr) {
            alert(xhr?.responseJSON?.message || "삭제 실패");
        });
    });

    bindTabs();
    loadDetail();

    // 탭의 평균 평점은 "전체 평균"으로 고정 표시
    loadReviewSummary();

    // 리뷰 탭 첫 진입 시 빠르게 보여주기 위한 0페이지 프리패치
    preloadReviewPage0();
});
