/**
 * venue/detail.js — 공연장 상세 페이지 (유저/관람객용)
 *
 * 공연자용 대관 폼과 동일하게 풍부한 공연장 정보(이미지, 타입, 주소, 연락처, 운영시간, 좌석 수,
 * 편의시설, 휴무일 등)를 카드로 표시. 대관 신청 기능은 없음.
 *
 * [API]
 * - GET /api/venues/{venueId} : 공연장 상세 (VenueDetailDto)
 * - GET /api/performances?venueId=&page=0&size=50 : 해당 공연장 공연 목록
 */
(function () {
    "use strict";

    const venueId = document.getElementById("venueId")?.value;
    if (!venueId) {
        console.error("[VenueDetail] venueId not found");
        return;
    }

    const $venueInfo = document.getElementById("venueInfo");
    const $perfSelect = document.getElementById("performanceSelect");
    const $btnSeatReview = document.getElementById("btnSeatReviewWrite");
    const $upcomingSection = document.getElementById("venueUpcomingSection");
    const $upcomingList = document.getElementById("venueUpcomingList");

    const DAY_KO = ["일", "월", "화", "수", "목", "금", "토"];
    const DAY_EN = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];

    function esc(str) {
        return String(str ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function buildClosingHtml(v) {
        var lines = [];
        if (v.regularClosingDays && v.regularClosingDays.length) {
            var dayNames = v.regularClosingDays.map(function (d) {
                var idx = DAY_EN.indexOf(d);
                return idx >= 0 ? DAY_KO[idx] + "요일" : d;
            });
            lines.push("정기 휴무: " + dayNames.join(", "));
        }
        if (v.temporaryClosingDates && v.temporaryClosingDates.length) {
            lines.push("임시 휴무: " + v.temporaryClosingDates.join(", "));
        }
        return lines.length ? "<p class=\"closing-info\"><i class=\"bi bi-x-circle\"></i> " + lines.join(" | ") + "</p>" : "";
    }

    /** 공연장 정보 로드 후 풍부한 카드 렌더 (공연자 대관 폼과 동일 구조) */
    function loadVenue() {
        fetch("/api/venues/" + venueId, { credentials: "include", headers: { Accept: "application/json" } })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                var d = body?.data;
                if (!d) {
                    $venueInfo.innerHTML = "<p class=\"text-danger p-4\">공연장 정보를 불러올 수 없습니다.</p>";
                    return;
                }
                var v = d;
                var typeMap = { CONCERT_HALL: "콘서트홀", THEATER: "극장", CLUB: "클럽", OUTDOOR: "야외", STUDIO: "스튜디오" };
                var typeLabel = typeMap[v.venueType] || (v.venueType || "기타");

                var imgHtml = v.venueImage
                    ? "<img src=\"" + esc(v.venueImage) + "\" alt=\"공연장 이미지\" class=\"venue-detail-image\"/>"
                    : "<div class=\"venue-detail-image-placeholder\"><i class=\"bi bi-building\"></i></div>";

                var facilitiesHtml = (v.facilities && v.facilities.length)
                    ? "<div class=\"facilities-wrap\">" + v.facilities.map(function (f) { return "<span class=\"facility-chip\">" + esc(f) + "</span>"; }).join("") + "</div>"
                    : "";

                var closingHtml = buildClosingHtml(v);

                $venueInfo.innerHTML =
                    "<div class=\"venue-detail-card\">" +
                    imgHtml +
                    "<div class=\"venue-detail-body\">" +
                    "<div class=\"venue-detail-name-row\">" +
                    "<span class=\"venue-detail-name\">" + esc(v.venueName) + "</span>" +
                    "<span class=\"venue-type-badge\">" + esc(typeLabel) + "</span>" +
                    "</div>" +
                    "<div class=\"venue-meta\">" +
                    (v.address ? "<div class=\"meta-row\"><i class=\"bi bi-geo-alt-fill\"></i>" + esc(v.address) + "</div>" : "") +
                    (v.contact ? "<div class=\"meta-row\"><i class=\"bi bi-telephone-fill\"></i>" + esc(v.contact) + "</div>" : "") +
                    (v.openTime ? "<div class=\"meta-row\"><i class=\"bi bi-clock-fill\"></i>운영시간: " + esc(v.openTime) + " ~ " + esc(v.closeTime || "") + "</div>" : "") +
                    (v.totalSeats ? "<div class=\"meta-row\"><i class=\"bi bi-people-fill\"></i>총 " + v.totalSeats + "석</div>" : "") +
                    (v.description ? "<div class=\"meta-row meta-row-desc\"><i class=\"bi bi-info-circle-fill\"></i><span>" + esc(v.description) + "</span></div>" : "") +
                    "</div>" +
                    (facilitiesHtml ? "<hr class=\"venue-divider\"/>" + facilitiesHtml : "") +
                    closingHtml +
                    "</div></div>";
            })
            .catch(function (err) {
                if (typeof console !== "undefined" && console.error) console.error("[VenueDetail] loadVenue failed", err);
                $venueInfo.innerHTML = "<p class=\"text-danger p-4\">공연장 정보를 불러올 수 없습니다.</p>";
            });
    }

    /** 해당 공연장 공연 목록 로드 후 드롭다운/리스트 채움 */
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
                    opt.textContent = esc(p.title || "공연 " + p.performanceId);
                    $perfSelect.appendChild(opt);
                });
                toggleSeatReviewButton();

                // 이 공연장에서 열리는 공연 리스트 UI
                if ($upcomingSection && $upcomingList) {
                    if (!list.length) {
                        $upcomingSection.style.display = "none";
                    } else {
                        const limited = list.slice(0, 5);
                        var html = limited.map(function (p) {
                            var id = p.performanceId;
                            if (!id) return "";
                            var title = esc(p.title || ("공연 " + id));
                            return "<a href=\"/performances/" + id + "\" class=\"venue-upcoming-card\">" +
                                "<div class=\"venue-upcoming-title\">" + title + "</div>" +
                                "</a>";
                        }).join("");
                        $upcomingList.innerHTML = html;
                        $upcomingSection.style.display = html ? "" : "none";
                    }
                }
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
