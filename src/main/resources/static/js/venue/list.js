/**
 * [Venue List Page Script] — 공연장 목록 단일 페이지 (프로필별 동작 분기)
 * - API: GET /api/venues?keyword=&page=&size=
 * - 응답: CommonResponse<Page<VenueListItemDto>>
 * - 카드 클릭: 공연자(ROLE_PERFORMER) → /venues/{id}/reservation (대관 신청), 유저 등 → /venues/{id} (상세, 좌석 리뷰)
 * - activeMode는 서버에서 #activeMode hidden 값으로 전달
 *
 * [HTML 전제]
 * - #venueSearchInput, #venueList, #venueEmpty, #activeMode(optional, 기본 ROLE_USER)
 */
(() => {
    const $input = document.getElementById("venueSearchInput");
    const $list = document.getElementById("venueList");
    const $empty = document.getElementById("venueEmpty");
    const $searchBtn = document.querySelector(".venue-search-btn");
    const $activeMode = document.getElementById("activeMode");

    if (!$input || !$list || !$empty) {
        console.error("[VenueList] Required elements not found. Check IDs: venueSearchInput, venueList, venueEmpty");
        return;
    }

    /** 공연자 프로필이면 대관 신청으로, 아니면 공연장 상세(좌석 리뷰)로 이동 */
    const isPerformerProfile = ($activeMode && $activeMode.value === "ROLE_PERFORMER");

    const DEFAULT_PAGE = 0;
    const DEFAULT_SIZE = Number($input?.dataset?.size || 10);

    let debounceTimer = null;

    /**
     * [보안/안정성] HTML Escape
     * - 서버에서 내려온 문자열을 그대로 innerHTML에 넣을 때 XSS 방지
     */
    function escapeHtml(str) {
        return String(str ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    /**
     * [표시용 라벨] VenueType enum 문자열을 한글로
     */
    function venueTypeLabel(type) {
        if (!type) return "기타";
        const map = {
            CONCERT_HALL: "콘서트홀",
            THEATER: "극장",
        };
        return map[type] || type;
    }

    /**
     * [렌더링] 카드 리스트 출력
     * - VenueListItemDto: venueId, venueName, address, venueType, totalSeats
     */
    function render(items) {
        $list.innerHTML = "";

        if (!items || items.length === 0) {
            $empty.classList.remove("d-none");
            return;
        }
        $empty.classList.add("d-none");

        const defaultImg = "/image/no-image.png";
        const html = items
            .map((v) => {
                const id = v.venueId;
                const name = escapeHtml(v.venueName);
                const addr = escapeHtml(v.address);
                const type = venueTypeLabel(v.venueType);
                const seats = v.totalSeats;
                const imgUrl = (v.imageUrl && String(v.imageUrl).trim() !== "") ? escapeHtml(v.imageUrl) : defaultImg;

                return `
                    <div class="venue-card" data-id="${id}">
                        <div class="venue-card-img-wrap">
                            <img src="${imgUrl}" alt="${name}" onerror="this.src='${defaultImg}'">
                        </div>
                        <div class="venue-card-body">
                            <h3 class="venue-title">${name}</h3>
                            <p class="venue-sub">${addr}</p>
                            <div class="venue-meta">
                                <span class="badge text-bg-dark">${escapeHtml(type)}</span>
                                ${seats != null ? `<span class="badge text-bg-secondary">총 ${escapeHtml(seats)}석</span>` : ""}
                            </div>
                        </div>
                    </div>
                `;
            })
            .join("");

        $list.insertAdjacentHTML("beforeend", html);

        // 카드 클릭: 프로필에 따라 대관 신청(공연자) 또는 공연장 상세(유저)
        document.querySelectorAll(".venue-card").forEach((el) => {
            el.addEventListener("click", () => {
                const venueId = el.getAttribute("data-id");
                if (!venueId) return;
                if (isPerformerProfile) {
                    window.location.href = `/venues/${venueId}/reservation`;
                } else {
                    window.location.href = `/venues/${venueId}`;
                }
            });
        });
    }

    /**
     * [API 호출]
     * - CommonResponse<Page<VenueListItemDto>>
     * - body.data.content 가 리스트
     */
    async function fetchVenues({ keyword = "", page = DEFAULT_PAGE, size = DEFAULT_SIZE }) {
        const params = new URLSearchParams();
        params.set("page", String(page));
        params.set("size", String(size));

        const trimmed = (keyword || "").trim();
        if (trimmed.length > 0) {
            params.set("keyword", trimmed);
        }

        const url = `/api/venues?${params.toString()}`;

        try {
            const res = await fetch(url, {
                headers: { Accept: "application/json" },
                credentials: "include",
            });

            if (!res.ok) {
                console.error("[VenueList] fetch failed", res.status);
                render([]);
                return;
            }

            const body = await res.json();

            // body.data = Page 객체, body.data.content = 리스트
            const pageObj = body?.data;
            const items = pageObj?.content || [];

            render(items);
        } catch (e) {
            console.error("[VenueList] fetch error", e);
            render([]);
        }
    }

    /**
     * [검색 트리거 - 디바운스]
     * - 입력 중 잦은 호출 방지
     * - 검색은 항상 0페이지부터
     */
    function onSearchDebounced() {
        const keyword = ($input.value || "").trim();

        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            fetchVenues({ keyword, page: 0, size: DEFAULT_SIZE });
        }, 250);
    }

    /**
     * [검색 트리거 - 즉시]
     * - 버튼 클릭/엔터 등 즉시 실행용
     */
    function onSearchImmediate() {
        const keyword = ($input.value || "").trim();
        clearTimeout(debounceTimer); // [추가] 버튼/엔터는 즉시 실행하니까 대기 타이머 제거
        fetchVenues({ keyword, page: 0, size: DEFAULT_SIZE });
    }

    // 입력 시 디바운스 검색
    $input.addEventListener("input", onSearchDebounced);

    // 엔터(Enter) 입력 시 즉시 검색 (부트스트랩 form 사용 시 리로드 방지)
    $input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            onSearchImmediate();
        }
    });

    // 검색 버튼 클릭 시 즉시 검색
    $searchBtn?.addEventListener("click", onSearchImmediate);

    // 첫 진입 - 전체 목록 0페이지 호출
    fetchVenues({ keyword: "", page: 0, size: DEFAULT_SIZE });
})();
