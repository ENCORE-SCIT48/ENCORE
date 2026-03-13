/**
 * performance/seatReviewWrite.js — 좌석 리뷰 작성/수정 페이지
 *
 * - 좌석에 위치(xPos/yPos)가 있으면 공연장 좌석 배치와 동일하게 캔버스에 표시하고 클릭으로 선택.
 * - 없으면 기존처럼 드롭다운으로만 선택.
 *
 * [API]
 * - GET /api/performances/{id} : 공연 정보
 * - GET /api/performances/{id}/seats : 좌석 목록 (xPos, yPos 또는 xRatio, yRatio 포함 시 배치도 표시)
 * - GET /api/performances/{id}/seat-reviews/{reviewId} : 수정 시 기존 리뷰 데이터
 * - POST /api/performances/{id}/seat-reviews : 작성
 * - PATCH /api/performances/{id}/seat-reviews/{reviewId} : 수정
 */
$(function () {
    "use strict";

    // 공연 ID: hidden input 우선, 없으면 URL 경로에서 추출 (/performances/9/reviews/seats/new)
    let performanceId = String($("#performanceId").val() || "").trim();
    if (!performanceId) {
        var match = window.location.pathname.match(/\/performances\/(\d+)\//);
        if (match) performanceId = match[1];
    }
    const reviewId = Number($("#reviewId").val() || 0);
    if (!performanceId) {
        console.error("[SeatReviewWrite] performanceId missing and could not parse from URL:", window.location.pathname);
        $("#perfTitle").text("공연 정보를 불러올 수 없습니다.");
        return;
    }
    // hidden에 넣어두면 뒤로가기 등에서 사용 가능
    $("#performanceId").val(performanceId);

    const SEAT_SIZE_RATIO = 0.05;
    const gradeColors = { vip: "#fbbf24", r: "#f87171", s: "#60a5fa", a: "#34d399" };

    let seatList = [];
    let floors = [];
    let currentFloorIdx = 0;
    let selectedSeatId = null;
    let canvas = null, ctx = null;

    /** 공연 제목·부가 정보 표시 (해당 공연 API에서 가져옴) */
    function loadPerformanceInfo() {
        $.ajax({
            url: `/api/performances/${performanceId}`,
            method: "GET",
            dataType: "json",
            xhrFields: { withCredentials: true }
        }).done(function (res) {
            const d = res?.data || res || {};
            const title = d.title != null ? String(d.title) : "";
            $("#perfTitle").text(title || "공연제목");

            const venueName = d.venueName != null ? String(d.venueName) : "-";
            const address = d.address != null ? String(d.address) : "-";
            const statusMap = { MUSICAL: "뮤지컬", PLAY: "연극", BAND: "밴드 공연" };
            const rawCategory = d.category != null ? d.category : (d.status != null ? d.status : "");
            const category = statusMap[rawCategory] || (rawCategory ? String(rawCategory) : "-");
            const year = d.productionYear != null ? String(d.productionYear) : (d.createdAt ? (new Date(d.createdAt).getFullYear()) : "-");
            $("#perfSub").text([address, venueName, category, year].join(" · "));
        }).fail(function () {
            $("#perfTitle").text("공연제목");
            $("#perfSub").text("공연 정보를 불러오지 못했습니다.");
        });
    }

    function loadSeats() {
        $.ajax({
            url: `/api/performances/${performanceId}/seats`,
            method: "GET",
            dataType: "json",
            xhrFields: { withCredentials: true }
        }).done(function (res) {
            const raw = res?.data != null ? res.data : res;
            const list = Array.isArray(raw) ? raw : [];
            seatList = list;

            const $sel = $("#seatId");
            $sel.prop("disabled", false);
            $sel.find("option:not(:first)").remove();
            if (list.length === 0) {
                $sel.after("<p class=\"text-muted small mt-2\" id=\"seatEmptyMsg\">이 공연에 공연장 정보가 없거나 등록된 좌석이 없어 저장은 불가합니다. 별점과 리뷰 내용은 입력할 수 있습니다.</p>");
                $("#submitBtn").prop("disabled", true);
                ensureStarsClickable();
                validate();
                return;
            }
            $sel.find("option:first").text("좌석을 선택해 주세요 " + list.length + "개");
            $("#seatSelectHint").show();
            list.forEach(function (s) {
                const id = s.seatId != null ? s.seatId : "";
                const label = [s.seatNumber, s.seatType, s.seatFloor != null ? s.seatFloor + "층" : ""].filter(Boolean).join(" · ") || "좌석 " + id;
                $sel.append($("<option></option>").attr("value", String(id)).text(label));
            });

            const withPosition = list.filter(function (s) {
                return (s.xPos != null && s.yPos != null) || (s.xRatio != null && s.yRatio != null);
            });
            if (withPosition.length > 0) {
                floors = buildFloors(list);
                $("#seatMapWrap").removeClass("d-none");
                initSeatCanvas();
                setTimeout(function () {
                    if (ctx && canvas && canvas.parentElement) {
                        canvas.width = canvas.parentElement.clientWidth;
                        canvas.height = canvas.parentElement.clientHeight;
                        drawSeats();
                    }
                }, 50);
            }
            ensureStarsClickable();
            validate();
        }).fail(function (xhr) {
            if (typeof console !== "undefined" && console.error) {
                console.error("[SeatReviewWrite] loadSeats failed", xhr?.status, xhr?.responseJSON);
            }
            $("#seatEmptyMsg").remove();
            $("#seatId").after("<p class=\"text-danger small mt-2\" id=\"seatEmptyMsg\">좌석 목록을 불러오지 못했습니다. 별점과 리뷰 내용은 입력할 수 있습니다.</p>");
            $("#submitBtn").prop("disabled", true);
            ensureStarsClickable();
        });
    }

    /** 좌석이 없어도 별점은 항상 눌러서 선택 가능하도록 보장 */
    function ensureStarsClickable() {
        $("#stars .star").prop("disabled", false).css({ "pointer-events": "auto", "cursor": "pointer" });
    }

    function buildFloors(list) {
        const byFloor = {};
        list.forEach(function (s) {
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

    function initSeatCanvas() {
        canvas = document.getElementById("seatCanvas");
        if (!canvas) return;
        ctx = canvas.getContext("2d");
        const wrapper = canvas.parentElement;
        if (!wrapper) return;

        function resize() {
            canvas.width = wrapper.clientWidth;
            canvas.height = wrapper.clientHeight;
            drawSeats();
        }
        resize();
        if (typeof ResizeObserver !== "undefined") {
            new ResizeObserver(resize).observe(wrapper);
        }

        var $container = $("#seatFloorTabs");
        $container.empty();
        if (floors.length > 1) {
            floors.forEach(function (f, i) {
                $container.append($("<button type='button' class='floor-btn'></button>")
                    .text(f.name).toggleClass("active", i === currentFloorIdx)
                    .on("click", function () {
                        currentFloorIdx = i;
                        $container.find(".floor-btn").removeClass("active").eq(i).addClass("active");
                        drawSeats();
                    }));
            });
        }

        canvas.addEventListener("click", function (e) {
            const rect = canvas.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            const w = canvas.width, h = canvas.height;
            const size = w * SEAT_SIZE_RATIO;
            const seats = floors[currentFloorIdx] ? floors[currentFloorIdx].seats : [];
            for (var i = seats.length - 1; i >= 0; i--) {
                var s = seats[i];
                var sx = s.xRatio * w, sy = s.yRatio * h;
                if (x >= sx && x <= sx + size && y >= sy && y <= sy + size) {
                    selectedSeatId = s.seatId;
                    $("#seatId").val(String(s.seatId));
                    drawSeats();
                    validate();
                    return;
                }
            }
        });
    }

    function drawSeats() {
        if (!ctx || !canvas || !floors[currentFloorIdx]) return;
        var w = canvas.width, h = canvas.height;
        ctx.clearRect(0, 0, w, h);
        var size = w * SEAT_SIZE_RATIO;
        var seats = floors[currentFloorIdx].seats;
        seats.forEach(function (s) {
            var sx = s.xRatio * w, sy = s.yRatio * h;
            ctx.fillStyle = gradeColors[s.seatType] || gradeColors.a;
            ctx.strokeStyle = selectedSeatId === s.seatId ? "#E63946" : "#333";
            ctx.lineWidth = selectedSeatId === s.seatId ? 2 : 1;
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

    /** 별점 UI 갱신 및 hidden 값 반영 */
    function renderStars(value) {
        $("#stars .star").each(function () {
            const v = Number($(this).data("value"));
            if (v <= value) $(this).addClass("is-on").text("★");
            else $(this).removeClass("is-on").text("☆");
        });
        $("#selectedRating").val(String(value));
        $("#ratingHint").text(value > 0 ? value + "점 선택됨" : "별점을 선택해 주세요");
        validate();
    }

    function validate() {
        const seatId = $("#seatId").val();
        const rating = Number($("#selectedRating").val() || 0);
        const content = ($("#content").val() || "").trim();
        $("#submitBtn").prop("disabled", !(seatId && rating > 0 && content.length >= 5));
    }

    // 별점: 위임으로 클릭 처리 (document에서 캡처해 #stars .star 클릭 확실히 처리)
    $(document).on("click", "#stars .star", function (e) {
        e.preventDefault();
        e.stopPropagation();
        var val = $(this).data("value");
        if (val != null) renderStars(Number(val));
    });
    $("#stars").on("mouseenter", ".star", function () {
        const value = Number($(this).data("value"));
        $("#stars .star").each(function () {
            const v = Number($(this).data("value"));
            $(this).text(v <= value ? "★" : "☆");
        });
    }).on("mouseleave", function () {
        renderStars(Number($("#selectedRating").val() || 0));
    });

    $("#seatId").on("change", function () {
        selectedSeatId = $(this).val() ? Number($(this).val()) : null;
        if (floors.length && canvas) {
            var sid = selectedSeatId;
            floors.forEach(function (f, i) {
                if (f.seats.some(function (s) { return s.seatId === sid; })) {
                    currentFloorIdx = i;
                    $("#seatFloorTabs .floor-btn").removeClass("active").eq(i).addClass("active");
                }
            });
            drawSeats();
        }
        validate();
    });
    $("#content").on("input change", validate);

    $("#backBtn").on("click", function (e) {
        e.preventDefault();
        if (window.history.length > 1) {
            history.back();
        } else {
            window.location.href = "/performances/" + performanceId + "?tab=seatReview";
        }
    });

    function loadReviewIfEditMode() {
        if (!reviewId) return;
        $.ajax({
            url: `/api/performances/${performanceId}/seat-reviews/${reviewId}`,
            method: "GET",
            dataType: "json",
            xhrFields: { withCredentials: true }
        }).done(function (res) {
            const d = res?.data || {};
            renderStars(Number(d.rating || 0));
            $("#content").val(d.content || "");
            var sid = d.seatId != null ? String(d.seatId) : "";
            $("#seatId").val(sid);
            selectedSeatId = sid ? Number(sid) : null;
            if (floors.length && selectedSeatId) {
                floors.forEach(function (f, i) {
                    if (f.seats.some(function (s) { return s.seatId === selectedSeatId; })) {
                        currentFloorIdx = i;
                        $("#seatFloorTabs .floor-btn").removeClass("active").eq(i).addClass("active");
                    }
                });
                drawSeats();
            }
            validate();
        }).fail(function (xhr) {
            if (typeof console !== "undefined" && console.error) {
                console.error("[SeatReviewWrite] loadReviewIfEditMode failed", xhr);
            }
            alert("리뷰 불러오기 실패");
        });
    }

    /** 작성/수정 제출 (POST 또는 PATCH) */
    $("#submitBtn").off("click").on("click", function () {
        const seatId = $("#seatId").val();
        const rating = Number($("#selectedRating").val() || 0);
        const content = ($("#content").val() || "").trim();
        const isEdit = !!reviewId;

        if (!seatId || rating < 1 || rating > 5 || content.length < 5) {
            alert("좌석 선택, 별점(1~5), 리뷰 5자 이상을 입력해 주세요.");
            return;
        }

        $("#submitBtn").prop("disabled", true);

        $.ajax({
            url: isEdit
                ? `/api/performances/${performanceId}/seat-reviews/${reviewId}`
                : `/api/performances/${performanceId}/seat-reviews`,
            method: isEdit ? "PATCH" : "POST",
            contentType: "application/json",
            data: JSON.stringify({ seatId: Number(seatId), rating, content }),
            xhrFields: { withCredentials: true }
        }).done(function () {
            window.location.href = `/performances/${performanceId}?tab=seatReview`;
        }).fail(function (xhr) {
            if (typeof console !== "undefined" && console.error) {
                console.error("[SeatReviewWrite] submit failed", xhr?.status, xhr?.responseJSON);
            }
            alert(xhr?.responseJSON?.message || "저장 실패");
            $("#submitBtn").prop("disabled", false);
            validate();
        });
    });

    renderStars(0);
    ensureStarsClickable();
    loadPerformanceInfo();
    loadSeats();
    loadReviewIfEditMode();
});
