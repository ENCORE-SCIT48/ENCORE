document.addEventListener("DOMContentLoaded", () => {
    const feedListEl = document.getElementById("feedList");
    const feedEmptyEl = document.getElementById("feedEmpty");

    if (!feedListEl) {
        console.error("[feed] #feedList element not found");
        return;
    }

    const params = new URLSearchParams(window.location.search);
    const userId = params.get("userId") || "8"; // 개발용 기본 8

    const apiUrl = userId ? `/api/feed?userId=${encodeURIComponent(userId)}` : "/api/feed";

    loadFeed(apiUrl)
    .then((items) => renderFeed(items, feedListEl, feedEmptyEl))
    .catch((err) => {
        console.error("[feed] load error:", err);
        renderFeed([], feedListEl, feedEmptyEl);
        // API 실패 시 사용자에게 안내 (빈 목록과 구분)
        if (feedEmptyEl) {
            feedEmptyEl.textContent = "피드를 불러오지 못했습니다. 서버 로그와 DB 설정·테스트 데이터(data-test-insert.sql)를 확인하세요.";
        }
    });
});

async function loadFeed(url) {
    const res = await fetch(url, {
        method: "GET",
        headers: { "Accept": "application/json" },
        credentials: "include",
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText} ${text}`);
    }

    const data = await res.json();
    return Array.isArray(data?.items) ? data.items : [];
}

function renderFeed(items, feedListEl, feedEmptyEl) {
    feedListEl.innerHTML = "";

    if (!items || items.length === 0) {
        if (feedEmptyEl) feedEmptyEl.classList.remove("d-none");
        return;
    }
    if (feedEmptyEl) feedEmptyEl.classList.add("d-none");

    // 최신순 정렬(시작시간 기준) - startTime이 null이면 뒤로
    items.sort((a, b) => {
        const ta = parseIsoLocalDateTime(a?.startTime)?.getTime() ?? Number.MAX_SAFE_INTEGER;
        const tb = parseIsoLocalDateTime(b?.startTime)?.getTime() ?? Number.MAX_SAFE_INTEGER;
        return ta - tb;
    });

    const fragment = document.createDocumentFragment();
    items.forEach((item) => fragment.appendChild(createFeedCard(item)));
    feedListEl.appendChild(fragment);
}

/**
 * 카드 1개 생성
 */
function createFeedCard(item) {
    const type = item?.type ?? "";
    const title = item?.title ?? "-";
    const message = item?.message ?? "";
    const startTime = item?.startTime ?? null;

    const startDate = parseIsoLocalDateTime(startTime);
    const timeText = startDate ? formatKoreanDateTime(startDate) : "시간 정보 없음";

    const { badgeText, subText } = buildLabels(item);

    const card = document.createElement("div");
    card.className = "feed-card";

    const badge = document.createElement("span");
    badge.className = "feed-badge";
    badge.textContent = badgeText;

    // 공연 제목
    const titleEl = document.createElement("div");
    titleEl.className = "feed-card-title";
    titleEl.textContent = title;

    // 팔로우 프로필 페이지 이동 영역
    const subEl = document.createElement("div");
    subEl.className = "feed-card-sub";
    subEl.textContent = subText;

    const msgEl = document.createElement("div");
    msgEl.className = "feed-card-msg";
    msgEl.textContent = message;

    const timeEl = document.createElement("div");
    timeEl.className = "feed-card-time";
    timeEl.textContent = timeText;

    const right = document.createElement("div");
    right.className = "feed-card-right";

    const left = document.createElement("div");
    left.className = "feed-card-left";
    left.appendChild(badge);
    left.appendChild(titleEl);
    left.appendChild(subEl);

    if (type !== "FOLLOW_WISHED" && message) left.appendChild(msgEl);
    left.appendChild(timeEl);

    card.appendChild(left);
    card.appendChild(right);

    const performanceId = item?.performanceId;

    if (performanceId) {
        card.style.cursor = "pointer";
        card.addEventListener("click", () => {
            window.location.href = `/performances/${performanceId}`;
        });
    }

    if (type === "FOLLOW_WISHED") {
        const actorUserId = item?.actorUserId;

        subEl.style.cursor = "pointer";

        subEl.addEventListener("click", (e) => {
            e.stopPropagation(); // 카드 클릭 막기

            if (!actorUserId) return;

            window.location.href = `/member/profile/u/${actorUserId}`;
        });
    }

    return card;
}

function buildLabels(item) {
    const type = item?.type ?? "";

    if (type === "UPCOMING_WISHED") {
    const m = item?.notifyBeforeMinutes ?? 30;
        return {
            badgeText: "다가오는 공연",
            subText: `내가 찜한 공연 · 시작 ${m}분 전`,
        };
    }

    if (type === "FOLLOW_WISHED") {
    const nick = item?.actorNickname ?? "팔로우";
        return {
            badgeText: "친구 추천",
            subText: `${nick} 님이 찜한 공연`,
        };
    }

    return {
        badgeText: "피드",
        subText: "",
    };
}

// "2026-02-23T14:47:24" / "2026-02-23 14:47:24" 둘 다 파싱되게
function parseIsoLocalDateTime(value) {
    if (!value) return null;

    // "YYYY-MM-DDTHH:mm:ss" -> JS Date OK(로컬로 처리되지만 브라우저마다 다를 수 있어 수동 파싱)
    // "YYYY-MM-DD HH:mm:ss" -> T로 치환
    const v = String(value).replace(" ", "T");

    // 수동 파싱(안전)
    const m = v.match(
    /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?/
    );
    if (!m) return null;

    const year = Number(m[1]);
    const month = Number(m[2]) - 1;
    const day = Number(m[3]);
    const hour = Number(m[4]);
    const min = Number(m[5]);
    const sec = Number(m[6] ?? "0");

    return new Date(year, month, day, hour, min, sec);
}

function formatKoreanDateTime(d) {
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    const hh = String(d.getHours()).padStart(2, "0");
    const mi = String(d.getMinutes()).padStart(2, "0");
    return `${mm}.${dd} ${hh}:${mi}`;
}
