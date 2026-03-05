/**
 * venue/detail.js — 공연장 상세 페이지
 *
 * [API]
 * - GET /api/venues/{venueId} : 공연장 정보
 * - GET /api/performances?venueId=&page=0&size=50 : 해당 공연장 공연 목록
 *
 * [동작]
 * - 공연장명·주소·연락처·설명 표시
 * - 공연 선택 드롭다운 채움 → "좌석 리뷰 작성" 클릭 시 /performances/{performanceId}/reviews/seats/new 이동
 *
 * [용도] 유저(관람객) 전용. 공연장 목록은 /venues 하나이며, 공연자 프로필일 때는 카드 클릭 시 대관 신청으로 이동.
 */
(function () {
    "use strict";

    const venueId = document.getElementById("venueId")?.value;
    if (!venueId) {
        console.error("[VenueDetail] venueId not found");
        return;
    }

    const $name = document.getElementById("venueName");
    const $meta = document.getElementById("venueMeta");
    const $desc = document.getElementById("venueDesc");
    const $perfSelect = document.getElementById("performanceSelect");
    const $btnSeatReview = document.getElementById("btnSeatReviewWrite");

    /** XSS 방지용 HTML 이스케이프 */
    function escapeHtml(str) {
        return String(str ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    /** 공연장 정보 로드 후 화면 반영 */
    function loadVenue() {
        fetch("/api/venues/" + venueId, { credentials: "include", headers: { Accept: "application/json" } })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                const d = body?.data;
                if (!d) return;
                $name.textContent = d.venueName ?? "공연장";
                const parts = [];
                if (d.address) parts.push(escapeHtml(d.address));
                if (d.contact) parts.push(escapeHtml(d.contact));
                $meta.textContent = parts.length ? parts.join(" · ") : "-";
                $desc.textContent = d.description ? escapeHtml(d.description) : "";
                if (typeof console !== "undefined" && console.debug) {
                    console.debug("[VenueDetail] venue loaded, venueId=" + venueId);
                }
            })
            .catch(function (err) {
                if (typeof console !== "undefined" && console.error) {
                    console.error("[VenueDetail] loadVenue failed", err);
                }
                $name.textContent = "공연장 정보를 불러올 수 없습니다.";
            });
    }

    /** 해당 공연장 공연 목록 로드 후 드롭다운 채움 */
    function loadPerformances() {
        const params = new URLSearchParams();
        params.set("venueId", venueId);
        params.set("page", "0");
        params.set("size", "50");

        fetch("/api/performances?" + params.toString(), {
            credentials: "include",
            headers: { Accept: "application/json" }
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                const content = body?.data?.content ?? body?.data ?? [];
                const list = Array.isArray(content) ? content : [];
                $perfSelect.innerHTML = "<option value=\"\">공연 선택</option>";
                list.forEach(function (p) {
                    const opt = document.createElement("option");
                    opt.value = p.performanceId;
                    opt.textContent = escapeHtml(p.title || "공연 " + p.performanceId);
                    $perfSelect.appendChild(opt);
                });
                toggleSeatReviewButton();
                if (typeof console !== "undefined" && console.debug) {
                    console.debug("[VenueDetail] performances loaded, count=" + list.length);
                }
            })
            .catch(function (err) {
                if (typeof console !== "undefined" && console.error) {
                    console.error("[VenueDetail] loadPerformances failed", err);
                }
                $perfSelect.innerHTML = "<option value=\"\">공연 목록 로드 실패</option>";
            });
    }

    /** 공연이 선택되었을 때만 "좌석 리뷰 작성" 버튼 활성화 */
    function toggleSeatReviewButton() {
        $btnSeatReview.disabled = !$perfSelect.value;
    }

    $perfSelect.addEventListener("change", toggleSeatReviewButton);

    $btnSeatReview.addEventListener("click", function () {
        const performanceId = $perfSelect.value;
        if (!performanceId) return;
        window.location.href = "/performances/" + performanceId + "/reviews/seats/new";
    });

    loadVenue();
    loadPerformances();
})();
