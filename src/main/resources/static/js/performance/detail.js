$(function () {
    const performanceId = $("#performanceId").val();
    const loginUserId = Number($("#loginUserId").val() || 0); // 내 리뷰 판별용

    let isWished = false;
    let isWatched = false;
    let isReported = false;
    let watchedChecked = false;
    let wishedChecked = false;
    let reportedChecked = false;

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

    // 좌석 리뷰 탭: 배치도 + 호버 툴팁
    let seatReviewLoaded = false;
    let seatReviewFloors = [];
    let seatReviewBySeat = {};
    let seatReviewCurrentFloor = 0;
    const SEAT_SIZE_RATIO = 0.05;
    const gradeColors = { vip: "#fbbf24", r: "#f87171", s: "#60a5fa", a: "#34d399" };

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

            if (tab === "seatReview" && !seatReviewLoaded) {
                loadSeatReviewMap();
            }
        });
    }

    function buildSeatReviewFloors(seatList) {
        const byFloor = {};
        seatList.forEach(function (s) {
            if ((s.xPos == null && s.xRatio == null) || (s.yPos == null && s.yRatio == null)) return;
            const f = s.seatFloor != null ? s.seatFloor : 1;
            if (!byFloor[f]) byFloor[f] = [];
            byFloor[f].push({
                seatId: s.seatId,
                seatNumber: s.seatNumber || "",
                seatType: (s.seatType || "a").toLowerCase(),
                xRatio: s.xRatio != null ? s.xRatio : (s.xPos / 1000),
                yRatio: s.yRatio != null ? s.yRatio : (s.yPos / 1000)
            });
        });
        return Object.keys(byFloor).sort(function (a, b) { return Number(a) - Number(b); }).map(function (f) {
            return { name: f + "층", seats: byFloor[f] };
        });
    }

    function loadSeatReviewMap() {
        seatReviewLoaded = true;
        $("#seatReviewListWrap").hide().find("#seatReviewList").empty();

        $.ajax({
            url: "/api/performances/" + performanceId + "/seats",
            method: "GET",
            dataType: "json",
            xhrFields: { withCredentials: true }
        })
            .done(function (resSeats) {
                const list = Array.isArray(resSeats?.data) ? resSeats.data : (Array.isArray(resSeats) ? resSeats : []);
                const withPos = list.filter(function (s) {
                    return (s.xPos != null && s.yPos != null) || (s.xRatio != null && s.yRatio != null);
                });

                function applySeatReviews(reviews) {
                    seatReviewBySeat = {};
                    (reviews || []).forEach(function (r) {
                        var sid = r.seatId != null ? r.seatId : null;
                        if (sid == null) return;
                        if (!seatReviewBySeat[sid]) seatReviewBySeat[sid] = [];
                        seatReviewBySeat[sid].push(r);
                    });
                }

                function loadBySeatThen(onSuccess) {
                    $.ajax({
                        url: "/api/performances/" + performanceId + "/seat-reviews/by-seat",
                        method: "GET",
                        dataType: "json",
                        xhrFields: { withCredentials: true }
                    })
                        .done(function (resReviews) {
                            const raw = resReviews?.data != null ? resReviews.data : resReviews;
                            const reviews = Array.isArray(raw) ? raw : [];
                            applySeatReviews(reviews);
                            if (onSuccess) onSuccess(reviews);
                        })
                        .fail(function () {
                            applySeatReviews([]);
                            if (onSuccess) onSuccess([]);
                        });
                }

                if (withPos.length === 0) {
                    $("#seatReviewMapWrap").addClass("d-none");
                    loadBySeatThen(function (reviews) {
                        if (reviews.length > 0) {
                            renderSeatReviewList(reviews);
                            $("#seatReviewListWrap").show();
                            $("#seatReviewEmpty").hide();
                        } else {
                            $("#seatReviewListWrap").hide();
                            $("#seatReviewEmpty").show();
                        }
                    });
                    return;
                }

                $("#seatReviewEmpty").hide();
                $("#seatReviewListWrap").hide();
                seatReviewFloors = buildSeatReviewFloors(list);
                loadBySeatThen(function () {
                    initSeatReviewCanvas();
                    drawSeatReviewCanvas();
                });
            })
            .fail(function () {
                $("#seatReviewMapWrap").addClass("d-none");
                $("#seatReviewListWrap").hide();
                $("#seatReviewEmpty").show();
            });
    }

    function renderSeatReviewList(reviews) {
        const $ul = $("#seatReviewList");
        $ul.empty();
        (reviews || []).forEach(function (r) {
            const seatLabel = [r.seatNumber, r.seatType, r.seatFloor != null ? r.seatFloor + "층" : ""].filter(Boolean).join(" · ") || "좌석";
            const content = (r.content || "").substring(0, 200) + ((r.content || "").length > 200 ? "…" : "");
            const dateStr = r.createdAt ? (typeof r.createdAt === "string" ? r.createdAt.substring(0, 10) : "") : "";
            var $li = $("<li class='list-group-item'></li>");
            var $row = $("<div class='d-flex justify-content-between align-items-start'></div>");
            $row.append($("<span class='fw-medium'>" + escapeHtml(seatLabel) + "</span>"));
            $row.append($("<span class='text-muted small'>★ " + (r.rating || "-") + "</span>"));
            $li.append($row);
            $li.append($("<p class='mb-0 mt-1 small text-secondary'>" + escapeHtml(content) + "</p>"));
            $li.append($("<span class='small text-muted'>" + escapeHtml(r.nickname || "-") + (dateStr ? " · " + dateStr : "") + "</span>"));
            $ul.append($li);
        });
    }

    function initSeatReviewCanvas() {
        const canvas = document.getElementById("seatReviewCanvas");
        if (!canvas) return;
        const ctx = canvas.getContext("2d");
        const wrapper = canvas.parentElement;
        if (!wrapper) return;

        function resize() {
            canvas.width = wrapper.clientWidth;
            canvas.height = wrapper.clientHeight;
            drawSeatReviewCanvas();
        }
        resize();
        if (typeof ResizeObserver !== "undefined") {
            new ResizeObserver(resize).observe(wrapper);
        }

        const $tabs = $("#seatReviewFloorTabs");
        $tabs.empty();
        if (seatReviewFloors.length > 1) {
            seatReviewFloors.forEach(function (f, i) {
                $tabs.append($("<button type='button' class='floor-btn'></button>").text(f.name).toggleClass("active", i === 0)
                    .on("click", function () {
                        seatReviewCurrentFloor = i;
                        $tabs.find(".floor-btn").removeClass("active").eq(i).addClass("active");
                        drawSeatReviewCanvas();
                    }));
            });
        }

        const $tip = $("#seatReviewTooltip");
        canvas.addEventListener("mousemove", function (e) {
            const rect = canvas.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            const w = canvas.width, h = canvas.height;
            const size = w * SEAT_SIZE_RATIO;
            const seats = seatReviewFloors[seatReviewCurrentFloor] ? seatReviewFloors[seatReviewCurrentFloor].seats : [];
            for (let i = seats.length - 1; i >= 0; i--) {
                const s = seats[i];
                const sx = s.xRatio * w, sy = s.yRatio * h;
                if (x >= sx && x <= sx + size && y >= sy && y <= sy + size) {
                    const arr = seatReviewBySeat[s.seatId];
                    if (arr && arr.length > 0) {
                        const r = arr[0];
                        const text = "★" + (r.rating || "-") + " · " + (r.content || "").substring(0, 80) + (r.content && r.content.length > 80 ? "…" : "");
                        $tip.html("<span class='tooltip-rating'>★" + (r.rating || "-") + "</span> " + (arr.length > 1 ? "외 " + (arr.length - 1) + "건<br>" : "") + "<span class='tooltip-content'>" + escapeHtml((r.content || "").substring(0, 120)) + (r.content && r.content.length > 120 ? "…" : "") + "</span>").show();
                        $tip.css({ left: (e.clientX + 15) + "px", top: (e.clientY + 10) + "px" });
                    }
                    return;
                }
            }
            $tip.hide();
        });
        canvas.addEventListener("mouseleave", function () { $tip.hide(); });
    }

    function drawSeatReviewCanvas() {
        const canvas = document.getElementById("seatReviewCanvas");
        if (!canvas) return;
        const ctx = canvas.getContext("2d");
        const w = canvas.width, h = canvas.height;
        ctx.clearRect(0, 0, w, h);
        if (!seatReviewFloors[seatReviewCurrentFloor]) return;
        const size = w * SEAT_SIZE_RATIO;
        const seats = seatReviewFloors[seatReviewCurrentFloor].seats;
        seats.forEach(function (s) {
            const sx = s.xRatio * w, sy = s.yRatio * h;
            const hasReview = seatReviewBySeat[s.seatId] && seatReviewBySeat[s.seatId].length > 0;
            ctx.fillStyle = gradeColors[s.seatType] || gradeColors.a;
            ctx.strokeStyle = hasReview ? "#2563eb" : "#333";
            ctx.lineWidth = hasReview ? 2 : 1;
            ctx.beginPath();
            if (ctx.roundRect) ctx.roundRect(sx, sy, size, size, size * 0.2);
            else ctx.rect(sx, sy, size, size);
            ctx.fill();
            ctx.stroke();
            ctx.fillStyle = "#000";
            ctx.font = "bold " + (size * 0.35) + "px sans-serif";
            ctx.textAlign = "center";
            ctx.textBaseline = "middle";
            ctx.fillText(s.seatNumber || s.seatId, sx + size / 2, sy + size / 2);
        });
    }

    function statusToCategory(categoryOrStatus) {
        const map = { MUSICAL: "뮤지컬", PLAY: "연극", BAND: "밴드 공연" };
        return map[categoryOrStatus] || categoryOrStatus || "-";
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
        if (on) {
            $("#chatCta").show();
        } else {
            $("#chatCta").hide();
        }
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
            const targetId = performanceId;
            const targetType = "PERFORMANCE";
            const targetName = $("#perfTitle").text() || "공연";

            const url =
                `/report?targetId=${encodeURIComponent(targetId)}` +
                `&targetType=${encodeURIComponent(targetType)}` +
                `&targetName=${encodeURIComponent(targetName)}`;

            window.location.href = url;
        });

        $("#writeReviewBtn").off("click").on("click", function () {
            // 리뷰 작성 페이지로 이동
            window.location.href = `/performances/${performanceId}/reviews/new`;
        });

        $("#chatCtaBtn").off("click").on("click", function () {
            window.location.href = `/performances/${performanceId}/chats`;
        });
    }

    function renderDetail(d) {
        const rawCategory = d?.category ?? d?.status; // 서버 category 우선, 없으면 구버전 status 폴백
        $("#categoryText").text(statusToCategory(rawCategory ?? "-"));
        $("#perfTitle").text(escapeHtml(d?.title ?? "공연 제목"));

        const venueName = d?.venueName ?? "-";
        const address = d?.address ?? "-";
        $("#metaText").text(`${address} · ${venueName}`);

        // 공연장 상세로 이동 가능한 경우: 메타 텍스트를 클릭 가능하게
        const venueId = d?.venueId;
        $("#metaText").off("click").removeClass("clickable");
        if (venueId) {
            $("#metaText")
                .addClass("clickable")
                .on("click", function () {
                    window.location.href = `/venues/${venueId}`;
                });
        }

        // 공연 대표 이미지(포스터): 있으면 표시, 없으면 플레이스홀더. 로드 실패 시에도 플레이스홀더
        const imageUrl = (d?.performanceImageUrl || "").trim();
        $("#posterImage").off("error").on("error", function () {
            $(this).hide().attr("src", "");
            $("#posterPlaceholder").show();
        });
        if (imageUrl) {
            $("#posterImage").attr("src", imageUrl).show();
            $("#posterPlaceholder").hide();
        } else {
            $("#posterImage").hide().attr("src", "");
            $("#posterPlaceholder").show();
        }

        // 평점은 "summary API"로만 세팅(정렬/페이지에 따라 흔들리지 않게)
        $("#descText").text(escapeHtml(d?.description ?? "공연상세설명"));

        // 연관 공연자/공연장 블록 토글
        const performerName = d?.performerStageName;
        if (performerName) {
            $("#relatedPerformerName").text(escapeHtml(performerName));
            $("#relatedPerformer").show();
        } else {
            $("#relatedPerformer").hide();
        }

        if (venueId) {
            $("#relatedVenue").show();
            $("#relatedVenueBtn")
                .off("click")
                .on("click", function () {
                    window.location.href = `/venues/${venueId}`;
                });
        } else {
            $("#relatedVenue").hide();
        }

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

    // 공연자 소유 여부에 따라 수정/삭제 버튼 노출
    function initOwnerActions() {
        if (!performanceId) return;
        $.ajax({
            url: `/api/performances/${performanceId}/ownership`,
            method: "GET",
            dataType: "json",
        })
            .done(function (res) {
                const editable = !!res?.data?.editable;
                if (!editable) return;
                const $actions = $("#perfOwnerActions");
                if (!$actions.length) return;
                $actions.show();

                $("#editPerformanceBtn")
                    .off("click")
                    .on("click", function () {
                        window.location.href = `/performances/${performanceId}/edit`;
                    });

                $("#deletePerformanceBtn")
                    .off("click")
                    .on("click", function () {
                        if (!confirm("이 공연을 삭제하시겠습니까? 삭제 후에는 되돌릴 수 없습니다.")) {
                            return;
                        }
                        $.ajax({
                            url: `/api/performances/${performanceId}`,
                            method: "DELETE",
                            dataType: "json",
                        })
                            .done(function () {
                                alert("공연이 삭제되었습니다.");
                                window.location.href = "/performances";
                            })
                            .fail(function (xhr) {
                                console.error("[PerformanceDetail] delete failed", xhr.status, xhr.responseText);
                                alert("공연 삭제에 실패했습니다. " + (xhr.responseJSON?.message || ""));
                            });
                    });
            })
            .fail(function () {
                // 권한 없거나 비로그인 등은 조용히 무시
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

    function ensureReportedStatus() {
        if (reportedChecked) return;

        $.ajax({
            url: `/api/performances/${performanceId}/reported`,
            method: "GET",
            dataType: "json",
            xhrFields: { withCredentials: true }
        })
            .done(function (res) {
                const reported = !!res?.data?.reported;
                setReportedUI(reported);
                reportedChecked = true;
            })
            .fail(function () {
                setReportedUI(false);
                reportedChecked = true;
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
        const encorePick = (r?.encorePick && String(r.encorePick).trim()) ? String(r.encorePick).trim() : "";

        $("#modalStars").text(stars);
        $("#modalMeta").text(`${nickname} · ${createdAt}`);
        if (encorePick) {
            $("#modalEncorePick")
                .text("🎵 Encore pick · " + encorePick)
                .attr("title", "클릭하면 공연 상단으로 이동")
                .show()
                .off("click")
                .on("click", function () {
                    reviewModal.hide();
                    setTimeout(function () {
                        document.getElementById("performanceHeader")?.scrollIntoView({ behavior: "smooth", block: "start" });
                    }, 300);
                });
        } else {
            $("#modalEncorePick").hide();
        }
        $("#modalContent").text(content);

        if (!reviewModal) {
            reviewModal = new bootstrap.Modal(document.getElementById("reviewDetailModal"));
        }
        reviewModal.show();
    }

    function scrollToPerformanceHeader() {
        document.getElementById("performanceHeader")?.scrollIntoView({ behavior: "smooth", block: "start" });
    }

    function buildReviewCard(r) {
        const nickname = escapeHtml(r?.nickname ?? "-");
        const rating = Number(r?.rating ?? 0);
        const content = escapeHtml(r?.content ?? "");
        const encorePick = (r?.encorePick && String(r.encorePick).trim()) ? escapeHtml(String(r.encorePick).trim()) : "";
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
                    ${encorePick ? `<div class="review-encore-pick js-encore-pick-scroll" role="button" tabindex="0" title="클릭하면 공연 상단으로 이동">🎵 Encore pick · ${encorePick}</div>` : ""}
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

    // Encore pick 클릭 -> 공연 상단으로 스크롤
    $(document).on("click", ".js-encore-pick-scroll", function (e) {
        e.stopPropagation();
        scrollToPerformanceHeader();
    });
    $(document).on("keydown", ".js-encore-pick-scroll", function (e) {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            e.stopPropagation();
            scrollToPerformanceHeader();
        }
    });

    // 리뷰 카드 클릭 -> 상세 모달 오픈
    $(document).on("click", ".review-card", function (e) {
        // 액션 버튼(수정/삭제/신고), Encore pick 눌렀으면 모달 열지 않음
        if ($(e.target).closest(".review-action-btn, .js-encore-pick-scroll").length) return;

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

        // 상세 → 좌석 리뷰 작성: 항상 현재 페이지의 performanceId로 이동 (진입 경로 보정)
        $("#seatReviewWriteLink").on("click", function (e) {
            var pid = $("#performanceId").val();
            if (pid) {
                e.preventDefault();
                window.location.href = "/performances/" + pid + "/reviews/seats/new";
            }
        });

        // URL에 ?tab=seatReview 있으면 해당 탭으로 복원 (좌석 리뷰 작성 후 돌아올 때)
        var tabParam = new URLSearchParams(window.location.search).get("tab");
        if (tabParam && ["desc", "review", "seatReview", "chat"].indexOf(tabParam) !== -1) {
            var $tabBtn = $(".tab-btn[data-tab='" + tabParam + "']");
            if ($tabBtn.length) $tabBtn.trigger("click");
        }

    loadDetail();
    initOwnerActions();
    ensureReportedStatus();

    // 탭의 평균 평점은 "전체 평균"으로 고정 표시
    loadReviewSummary();

    // 리뷰 탭 첫 진입 시 빠르게 보여주기 위한 0페이지 프리패치
    preloadReviewPage0();
});
