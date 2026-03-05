/**
 * performance/seatReviewWrite.js — 좌석 리뷰 작성/수정 페이지
 *
 * [API]
 * - GET /api/performances/{id} : 공연 정보
 * - GET /api/performances/{id}/seats : 좌석 목록(드롭다운)
 * - GET /api/performances/{id}/seat-reviews/{reviewId} : 수정 시 기존 리뷰 데이터
 * - POST /api/performances/{id}/seat-reviews : 작성
 * - PATCH /api/performances/{id}/seat-reviews/{reviewId} : 수정
 *
 * [전제] #performanceId, #reviewId(hidden), #seatId, #stars, #content, #submitBtn, #backBtn
 */
$(function () {
    "use strict";

    const performanceId = $("#performanceId").val();
    const reviewId = Number($("#reviewId").val() || 0);

    /** 공연 제목·부가 정보 표시 */
    function loadPerformanceInfo() {
        $.ajax({
            url: `/api/performances/${performanceId}`,
            method: "GET",
            dataType: "json"
        }).done(function (res) {
            const d = res?.data || {};
            $("#perfTitle").text(d?.title ?? "공연제목");
            const venueName = d?.venueName ?? "-";
            const address = d?.address ?? "-";
            const statusMap = { MUSICAL: "뮤지컬", PLAY: "연극", BAND: "밴드 공연" };
            const rawCategory = d?.category ?? d?.status; // 서버 category 우선, 없으면 status 폴백
            const category = statusMap[rawCategory] ?? (rawCategory ?? "-");
            const year = d?.productionYear ?? "-";
            $("#perfSub").text(`${address} · ${venueName} · ${category} · ${year}`);
        });
    }

    function loadSeats() {
        $.ajax({
            url: `/api/performances/${performanceId}/seats`,
            method: "GET",
            dataType: "json"
        }).done(function (res) {
            const list = res?.data || [];
            const $sel = $("#seatId");
            $sel.find("option:not(:first)").remove();
            list.forEach(function (s) {
                const label = [s.seatNumber, s.seatType, s.seatFloor != null ? s.seatFloor + "층" : ""].filter(Boolean).join(" · ") || "좌석 " + s.seatId;
                $sel.append($("<option></option>").attr("value", s.seatId).text(label));
            });
            validate();
        }).fail(function (xhr) {
            if (typeof console !== "undefined" && console.error) {
                console.error("[SeatReviewWrite] loadSeats failed", xhr);
            }
            alert("좌석 목록을 불러오지 못했습니다.");
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

    $("#stars").on("click", ".star", function () {
        renderStars(Number($(this).data("value")));
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

    $("#seatId, #content").on("input change", validate);

    $("#backBtn").on("click", function () {
        history.back();
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
            $("#seatId").val(d.seatId != null ? String(d.seatId) : "");
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
    loadPerformanceInfo();
    loadSeats();
    loadReviewIfEditMode();
});
